package gov.va.ascent.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import gov.va.ascent.tools.utils.Out;
import gov.va.ascent.tools.utils.Severity;
import gov.va.ascent.tools.versions.PomVersionsParser;
import gov.va.ascent.tools.versions.model.Version;

/**
 * Make a report of explicit for explicit version references found in project pom.xml files.
 * <p>
 * It is assumed that all projects are cloned into one directory,
 * and a GIT_HOME environment property is defined.
 * <p>
 * java -cp target/ascent-developer.jar gov.va.ascent.tools.[package].Classname .
 *
 * @author aburkholder
 */
public class Versions {

	/** Constant for space character */
	private static final String SPACE = " ";
	/** Constant for desired max line length */
	private static final int LINELEN = 79;
	/** Property name for the nexus project base url */
	private static final String PROPS_NEXUS = "versions.nexus.base-projects-url";
	/** Property name for the report output file */
	private static final String PROPS_REPORTFILE = "versions.report.output-file";
	/** Property name for any second level projects to be processed */
	private static final String PROPS_2NDLEVEL = "versions.projects.second-level";

	/** Name of the properties file on the classpath */
	private static final String PROPERTIES_FILENAME = "versions.properties";

	/** The path to the git directory */
	private String gitHomePath;

	/** The base Nexus URL for Ascent and VetServices projects */
	String nexusUrl;
	/** The name of the report file */
	String reportFile = "VersionsReport.txt";
	/** The names of additional projects to process */
	Map<String, List<String>> extraProjects = new HashMap<>();

	/** All the collection versioning information */
	private Map<String, Version> versions = new TreeMap<>(Comparator.comparing(String::toString));

	/**
	 * Do not instantiate
	 */
	private Versions() {
		// noop
	}

	/**
	 * Run the Versions program from the ascent-developer directory:<br/>
	 * {@code java -cp target/ascent-developer.jar gov.va.ascent.tools.versioning.Versions .}
	 *
	 * @param args - arguments passed in from the command line
	 */
	public static void main(String[] args) {
		new Versions().runMe();
	}

	/**
	 * Entry point for running the program in the instantiated Versions object.
	 */
	private void runMe() {
		readGitHomePath();
		readProperties();
		versions = new PomVersionsParser(this.versions, this.extraProjects, this.gitHomePath)
				.processProjectDirectories();
		printReport();
	}

	/**
	 * Retrieve the GIT_HOME property from the host OS environment variables.
	 */
	private void readGitHomePath() {
		gitHomePath = System.getenv("GIT_HOME");
		if (StringUtils.isBlank(gitHomePath)) {
			Out.println(0, Severity.ERROR,
					"Could not find environment variable \"GIT_HOME\". Please set the variable to your git directory and try again.");
			throw new RuntimeException();
		}
	}

	private void readProperties() {
		Properties props = new Properties();
		try (InputStream in = Files.newInputStream(
				Paths.get(ClassLoader.getSystemResource(PROPERTIES_FILENAME).toURI()))) {
			props.load(in);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("While reading versions.properties", e);
		}

		nexusUrl = props.getProperty(PROPS_NEXUS);
		if (StringUtils.isBlank(nexusUrl)) {
			throw new RuntimeException("Cannot have empty " + PROPS_NEXUS + " property in versions.properties.");
		}
		reportFile = props.getProperty(PROPS_REPORTFILE) == null ? reportFile : props.getProperty(PROPS_REPORTFILE);

		String secondLevel = props.getProperty(PROPS_2NDLEVEL);
		if (!StringUtils.isBlank(secondLevel)) {
			String[] chunks = secondLevel.split(",");
			if (chunks != null) {
				for (String chunk : chunks) {
					String[] projectChunks = chunk.split("\\[");
					String baseProject = projectChunks[0];
					String[] subProjects = projectChunks[1].replaceAll("]", "").split("\\|");

					extraProjects.put(baseProject, Arrays.asList(subProjects));
				}
			}
		}
	}

	private void printReport() {
		Out.println("");
		Out.println(StringUtils.repeat("=", LINELEN));
		Out.println("Version Report for " + gitHomePath);

		versions.forEach(this::printVersion);
		Out.println("");
	}

	/**
	 * Print report rows of the collected version info for a specific project.
	 *
	 * @param projectPath - disc path to the project
	 * @param rootElement - the &lt;project&gt; element
	 */
	private void printVersion(String projectPath, Version rootElement) {
		Out.println("");
		Out.println(StringUtils.repeat("-", LINELEN));

		if (rootElement != null) {
			Out.println(rootElement.getArtifactId() + SPACE + rootElement.getVersion());
			if (rootElement.getParent() != null) {
				Version parent = rootElement.getParent();
				Out.println(1,
						(parent.getHierarchyIdTag() == null ? "" : parent.getHierarchyIdTag().getOutputPrefix())
								+ parent.getArtifactId()
								+ SPACE + parent.getVersion());
			}
			if (rootElement.getDependencies() != null) {
				for (Version dep : rootElement.getDependencies()) {
					if (dep != null) {
						Out.println(1,
								(dep.getHierarchyIdTag() == null ? "" : dep.getHierarchyIdTag().getOutputPrefix())
										+ dep.getArtifactId() + SPACE + dep.getVersion());
					}
				}
			}
		}
	}
}
