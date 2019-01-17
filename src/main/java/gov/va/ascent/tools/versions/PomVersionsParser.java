package gov.va.ascent.tools.versions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.va.ascent.tools.utils.Out;
import gov.va.ascent.tools.utils.Severity;
import gov.va.ascent.tools.versions.model.PomTags;
import gov.va.ascent.tools.versions.model.Version;

/**
 * Processes POM files from projects found directly under the GIT_HOME directory,
 * and any additional second-level subprojects declared in versions.properties.
 *
 * @author aburkholder
 */
public class PomVersionsParser {

	/** A map of &lt;relativeProjectPath, versionInfo&gt; */
	private Map<String, Version> versions;
	/** A map of &lt;baseProjectName, List&lt;subProjectName&gt;&gt; */
	private Map<String, List<String>> extraProjects;
	/** The value of system GIT_HOME environment variable */
	private String gitHomePath;

	/**
	 * Processes POM files from projects found directly under the GIT_HOME directory,
	 * and any additional second-level subprojects declared in versions.properties.
	 *
	 * @param versions - the map in which to put version information for the caller
	 * @param extraProjects - any sub-projects that need to report version information
	 * @param gitHomePath - the value of system GIT_HOME environment variable
	 */
	public PomVersionsParser(Map<String, Version> versions, Map<String, List<String>> extraProjects, String gitHomePath) {
		this.versions = versions;
		this.extraProjects = extraProjects;
		this.gitHomePath = gitHomePath;
	}

	/**
	 * The hub method for processing all project directories
	 *
	 * @return Map of &lt;relativeProjectPath, versionInfo&gt;
	 */
	public Map<String, Version> processProjectDirectories() {
		Path path = Paths.get(gitHomePath);
		// process each directory entry immediately under GIT_HOME
		try (Stream<Path> stream = Files.list(path)) {
			stream.filter(path1 -> path1.toFile().isDirectory())
					.forEach(t -> {
						try {
							processProject(t); // do it
						} catch (ParserConfigurationException | SAXException | IOException e) {
							Out.println(0, Severity.ERROR, "While processing project \"" + t.normalize().toString() + "\"", e);
						}
					});

		} catch (IOException e) {
			Out.println(0, Severity.ERROR, "While getting stream for \"" + path.toAbsolutePath().normalize().toString() + "\"", e);
		}
		return versions;
	}

	/**
	 * Process a project directory.
	 *
	 * @param projectPath - the disc path to the project root directory
	 * @throws ParserConfigurationException - problem configuring new DocumentBuilderFactory
	 * @throws IOException - problem creating DocumentBuilder or parsing Document
	 * @throws SAXException - problem creating DocumentBuilder or parsing Document
	 */
	private void processProject(Path projectPath) throws ParserConfigurationException, SAXException, IOException {
		projectPath = projectPath.toAbsolutePath().normalize();
		File projectPom = Paths.get(projectPath.toString(), "pom.xml").toFile();

		if (projectPom.exists() && projectPom.canRead()) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setIgnoringComments(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(projectPom);

			Element rootElement = doc.getDocumentElement();
			rootElement.normalize();

			Version project = getVersion(null, rootElement);
			if (project != null) {
				project.setParent(getParent(rootElement));
				project.getDependencies().addAll(getDependencies(null, rootElement));
				project.getDependencies().addAll(getManagedDependencies(rootElement));

				versions.put(projectPath.toString(), project);

				// path.getFileName() just returns the last part of the path - it is the project name in this case
				if (extraProjects.containsKey(projectPath.getFileName().toString())) {
					for (String subproject : extraProjects.get(projectPath.getFileName().toString())) {
						Path newpath = Paths.get(projectPath.toString(), subproject);
						/*
						 * Make a recursive call to this method to process the sub-project.
						 *
						 * NOTE that this allows for any level of sub-projects to be declared
						 * in the properties simply by adding their name as a subproject
						 * to the versions.projects.second-level property in versions.properties.
						 * Listed sub-projects are processed in linear order, so subproject hierarchies
						 * must be listed in order of their hierarchy so they will be found.
						 */
						processProject(newpath);
					}
				}

			} else {
				Out.println(0, Severity.WARN,
						"Could not find <" + rootElement.getNodeName() + "><version> element in " + projectPom.toString());
			}

		} else {
			Out.println(0, Severity.WARN,
					"Cannot read pom.xml in " + projectPom.toString());
		}
	}

	/**
	 * Find a single node of a given name (first found is returned) in the node list.
	 *
	 * @param list the NodeList to search
	 * @param nodeName the name of the node to search for
	 * @return Node - the found node, or {@code null}
	 */
	private Node findElement(NodeList list, String nodeName) {
		if (list == null || list.getLength() < 1 || StringUtils.isBlank(nodeName)) {
			return null;
		}

		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE
					&& nodeName.equals(item.getNodeName())) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Find all nodes with a given name in the node list.
	 *
	 * @param list the NodeList to search
	 * @param nodeName the name of the node(s) to search for
	 * @return List&lt;Node&gt; - the found nodes, or {@code null}
	 */
	private List<Node> findElements(NodeList list, String nodeName) {
		if (list == null || list.getLength() < 1 || StringUtils.isBlank(nodeName)) {
			return null;
		}

		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE
					&& nodeName.equals(item.getNodeName())) {
				nodes.add(item);
			}
		}
		return nodes.isEmpty() ? null : nodes;
	}

	/**
	 * Get the version information from the specified node.
	 * <p>
	 * The hierarchyIdTag should be {@code null} for root &lt;project&gt; version info,
	 * or populated with a meaningful tag from a parent tag in the hierarchy.
	 * The intent is to clearly identify the origin of the version information.
	 * Typically, this will be the tag closest to the root &lt;project&gt; tag.
	 * For example, hierarchyIdTag should be:
	 * <ul>
	 * <table style="border-collapse: collapse; border: 1px solid gray;">
	 * <tr style="border: 1px solid gray;text-align: left;">
	 * <th>Param Value</th>
	 * <th></th>
	 * <th>Version Location</th>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">{@code null}</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style="vertical-align:top">&lt;project&gt;&lt;version&gt;</td>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">&lt;parent&gt;</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style="vertical-align:top">&lt;project&gt;&lt;parent&gt;&lt;version&gt;</td>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">&lt;dependencies&gt;</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style="vertical-align:top">&lt;project&gt;&lt;dependencies&gt;&lt;dependency&gt;&lt;version&gt;</td>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">&lt;dependencyManagement&gt;</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style=
	 * "vertical-align:top">&lt;project&gt;&lt;dependencyManagement&gt;&lt;dependencies&gt;&lt;dependency&gt;&lt;version&gt;</td>
	 * </tr>
	 * </table>
	 * </ul>
	 *
	 * @param hierarchyIdTag - {@code null} or the non-root parent element
	 * @param rootElement - the element containing version tags
	 * @return Version - the version object for the element, or {@code null}
	 */
	private Version getVersion(PomTags hierarchyIdTag, Node rootElement) {
		Node groupId = findElement(rootElement.getChildNodes(), PomTags.TAG_GROUP_ID);
		Node artifactId = findElement(rootElement.getChildNodes(), PomTags.TAG_ARTIFACT_ID);
		Node version = findElement(rootElement.getChildNodes(), PomTags.TAG_VERSION);

		Version ret = null;
		if (version != null) {
			String text = version.getTextContent();
			if (!StringUtils.isBlank(text) && Pattern.matches("[0-9]*\\.[0-9]*\\.[0-9]*.*", text)) {
				ret = new Version(hierarchyIdTag,
						groupId == null ? "null" : groupId.getTextContent(),
						artifactId == null ? "null" : artifactId.getTextContent(),
						text);
			}
		}
		return ret;
	}

	/**
	 * Get the version information for the parent project.
	 *
	 * @param rootElement - the &lt;parent&gt; element
	 * @return Version - the version object for the parent
	 */
	private Version getParent(Node rootElement) {
		Node parent = findElement(rootElement.getChildNodes(), PomTags.PARENT.getTagName());
		return parent == null ? null : getVersion(PomTags.PARENT, parent);
	}

	/**
	 * Get the version information for dependencies.
	 * <p>
	 * The hierarchyIdTag should be {@code null} for root &lt;project&gt; version info,
	 * or populated with a meaningful tag from a parent tag in the hierarchy.
	 * The intent is to clearly identify the origin of the version information.
	 * Typically, this will be the tag closest to the root &lt;project&gt; tag.
	 * For example, hierarchyIdTag should be:
	 * <ul>
	 * <table style="border-collapse: collapse; border: 1px solid gray;">
	 * <tr style="border: 1px solid gray;text-align: left;">
	 * <th>Param Value</th>
	 * <th></th>
	 * <th>Version Location</th>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">{@code null}</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style="vertical-align:top">&lt;project&gt;&lt;version&gt;</td>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">&lt;parent&gt;</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style="vertical-align:top">&lt;project&gt;&lt;parent&gt;&lt;version&gt;</td>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">&lt;dependencies&gt;</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style="vertical-align:top">&lt;project&gt;&lt;dependencies&gt;&lt;dependency&gt;&lt;version&gt;</td>
	 * </tr>
	 * <tr style="border: 1px solid gray;">
	 * <td style="vertical-align:top">&lt;dependencyManagement&gt;</td>
	 * <td style="vertical-align:top">&nbsp;for&nbsp;</td>
	 * <td style=
	 * "vertical-align:top">&lt;project&gt;&lt;dependencyManagement&gt;&lt;dependencies&gt;&lt;dependency&gt;&lt;version&gt;</td>
	 * </tr>
	 * </table>
	 * </ul>
	 *
	 * @param hierarchyIdTag - {@code null} or the non-root parent element
	 * @param rootElement - the &lt;dependencies&gt; element
	 * @return List&lt;Version&gt; - the version objects for the dependencies, or empty list
	 */
	private List<Version> getDependencies(PomTags hierarchyIdTag, Node rootElement) {
		List<Version> list = new ArrayList<>();

		Node node = findElement(rootElement.getChildNodes(), PomTags.DEPENDENCIES.getTagName());
		if (node == null || !node.hasChildNodes()) {
			return list;
		}
		if (hierarchyIdTag == null) {
			hierarchyIdTag = PomTags.fromTagName(node.getNodeName());
		}

		List<Node> nodes = findElements(node.getChildNodes(), "dependency");
		if (nodes == null || nodes.isEmpty()) {
			return list;
		}

		for (Node item : nodes) {
			Version v = getVersion(hierarchyIdTag, item);
			if (v != null) {
				list.add(v);
			}
		}

		if (!list.isEmpty()) {
			list.sort(Comparator.comparing(Version::getGroupId)
					.thenComparing(Version::getArtifactId)
					.thenComparing(Version::getVersion));
		}
		return list;
	}

	/**
	 * Get the version information for dependencyManagement dependencies.
	 *
	 * @param rootElement - the &lt;dependencyManagement&gt; element
	 * @return List&lt;Version&gt; - the version objects for the dependencies, or empty list
	 */
	private List<Version> getManagedDependencies(Node rootElement) {
		Node node = findElement(rootElement.getChildNodes(), PomTags.DEPENDENCY_MANAGEMENT.getTagName());
		if (node != null) {
			return getDependencies(PomTags.fromTagName(node.getNodeName()), node);
		}
		return new ArrayList<Version>();
	}
}
