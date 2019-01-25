package gov.va.ascent.tools.versions.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import gov.va.ascent.tools.versions.RevisionParser;

/**
 * Data model object for explicit version references found in project pom.xml files.
 *
 * @author aburkholder
 */
public class Version {

	/** the relative path of the project from GIT_HOME, e.g. vetservices-refdata */
	private Path projectPath;

	/** {@code null} or the non-root parent element from the POM */
	private PomTags hierarchyIdTag;
	/** the groupId for the artifact version from the POM */
	private String groupId;
	/** the artifactId for the artifact version from the POM */
	private String artifactId;
	/** the explicit version declared for the artifact from the POM */
	private String version;
	/** the {@link Revision} information for the version */
	private Revision revision;

	/** {@code true} if the artifact exists in nexus */
	private Boolean exists;

	/** A Version object for the parent project */
	private Version parent;
	/** A non-null list of Version objects for project dependencies */
	private List<Version> dependencies = new ArrayList<>();

	/**
	 * Create an empty Version data model object.
	 */
	public Version() {
		// noop
	}

	/**
	 * Create a populated Version data model object for one row of console output.
	 * <p>
	 * The {@code projectPath} parameter must be the relative path of the project from GIT_HOME,
	 * not the absolute path.
	 * Examples:
	 * <ul>
	 * <li>{@code $GIT_HOME/vetservices-refdata} must be passed as {@code vetservices-refdata}<br/>
	 * <li>{@code $GIT_HOME/ascent-platform/ascent-platform-parent} must be passed as {@code ascent-platform/ascent-platform-parent}
	 * </ul>
	 * <p>
	 * The {@code hierarchyIdTag} parameter should be {@code null} or populated with a meaningful tag from the POMs parent tag in the
	 * hierarchy.
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
	 * @param projectPath - the relative path from GIT_HOME for the project
	 * @param hierarchyIdTag - {@code null} or the non-root parent element from the POM
	 * @param groupId - the groupId for the artifact version
	 * @param artifactId - the artifactId for the artifact version
	 * @param version - the explicit version declared for the artifact
	 */
	public Version(Path projectPath, PomTags hierarchyIdTag, String groupId, String artifactId, String version, Boolean exists) {
		this.hierarchyIdTag = hierarchyIdTag;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.exists = exists;
		this.revision = RevisionParser.parseRevision(version);
	}

	/**
	 * The {@code projectPath} parameter must be the relative path of the project from GIT_HOME,
	 * not the absolute path.
	 * Examples:
	 * <ul>
	 * <li>{@code $GIT_HOME/vetservices-refdata} must be passed as {@code vetservices-refdata}<br/>
	 * <li>{@code $GIT_HOME/ascent-platform/ascent-platform-parent} must be passed as {@code ascent-platform/ascent-platform-parent}
	 * </ul>
	 *
	 * @return Path - the relative path of the project
	 */
	public Path getProjectPath() {
		return projectPath;
	}

	/**
	 * The {@code projectPath} parameter must be the relative path of the project from GIT_HOME,
	 * not the absolute path.
	 * Examples:
	 * <ul>
	 * <li>{@code $GIT_HOME/vetservices-refdata} must be passed as {@code vetservices-refdata}<br/>
	 * <li>{@code $GIT_HOME/ascent-platform/ascent-platform-parent} must be passed as {@code ascent-platform/ascent-platform-parent}
	 * </ul>
	 *
	 * @param projectPath - the relative path of the project
	 */
	public void setProjectPath(Path projectPath) {
		this.projectPath = projectPath;
	}

	/**
	 * Will be {@code null} for root &lt;project&gt; version info,
	 * or will have a tag from the parent hierarchy of the version info.
	 *
	 * @return the hierarchyIdTag
	 */
	public PomTags getHierarchyIdTag() {
		return hierarchyIdTag;
	}

	/**
	 * The hierarchyIdTag should be {@code null} for root &lt;project&gt; version info,
	 * or populated with a meaningful tag from the POMs parent tag in the hierarchy.
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
	 * @param hierarchyIdTag the hierarchyIdTag to set
	 */
	public void setHierarchyIdTag(PomTags hierarchyIdTag) {
		this.hierarchyIdTag = hierarchyIdTag;
	}

	/**
	 * The &lt;groupId&gt; for the artifact version.
	 *
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * The &lt;groupId&gt; for the artifact version.
	 *
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * The &lt;artifactId&gt; for the artifact version.
	 *
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * The &lt;artifactId&gt; for the artifact version.
	 *
	 * @param artifactId the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * The explicit &lt;version&gt; declared for the artifact.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * The explicit &lt;version&gt; declared for the artifact.
	 *
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * The decomposed &lt;version&gt; as a {@link Revision} object.
	 *
	 * @return the revision
	 */
	public Revision getRevision() {
		return revision;
	}

	/**
	 * The version info for the &lt;parent&gt;.
	 *
	 * @return the parent
	 */
	public Version getParent() {
		return parent;
	}

	/**
	 * The version info for the &lt;parent&gt;.
	 *
	 * @param parent the parent to set
	 */
	public void setParent(Version parent) {
		this.parent = parent;
	}

	/**
	 * The version info for any &lt;project&gt;&lt;dependencies&gt;
	 * or &lt;dependencyManagement&gt;&lt;dependencies&gt;.
	 * <p>
	 * Should never be {@code null}, but may be empty.
	 *
	 * @return the dependencies
	 */
	public List<Version> getDependencies() {
		return dependencies;
	}

	/**
	 * Does the artifact version exist in nexus?
	 * <ul>
	 * <li>{@code null} = unknown or not applicable (e.g. subprojects)
	 * <li>{@code true} = artifact directory exists in nexus
	 * <li>{@code false} = artifact directory not found in nexus
	 * </ul>
	 *
	 * @return true if artifact version exists in nexus
	 */
	public Boolean exists() {
		return exists;
	}

	/**
	 * Does the artifact version exist in nexus
	 *
	 * @param exists - if artifact version exists in nexus
	 */
	public void setExists(Boolean exists) {
		this.exists = exists;
	}
}
