package gov.va.ascent.tools.versions.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model object for explicit version references found in project pom.xml files.
 *
 * @author aburkholder
 */
public class Version {

	PomTags hierarchyIdTag;
	String groupId;
	String artifactId;
	String version;

	Version parent;
	List<Version> dependencies = new ArrayList<>();

	/**
	 * Create an empty Version data model object.
	 */
	public Version() {
		// noop
	}

	/**
	 * Create a populated Version data model object for one row of console output.
	 * <p>
	 * The hierarchyIdTag should be {@code null} or populated with a meaningful tag from a parent tag in the hierarchy.
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
	 * @param groupId - the groupId for the artifact version
	 * @param artifactId - the artifactId for the artifact version
	 * @param version - the version of the artifact
	 */
	public Version(PomTags hierarchyIdTag, String groupId, String artifactId, String version) {
		this.hierarchyIdTag = hierarchyIdTag;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
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
	 * The &lt;version&gt; of the artifact.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * The &lt;version&gt; of the artifact.
	 *
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
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
}
