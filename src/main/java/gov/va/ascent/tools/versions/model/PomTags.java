package gov.va.ascent.tools.versions.model;

/**
 * POM tags in pom.xml that are used to determine versions and version dependencies.
 *
 * @author aburkholder
 */
public enum PomTags {

	/** The &lt;project&gt; tag */
	PROJECT("project", 0, ""),
	/** The &lt;parent&gt; tag */
	PARENT("parent", 1, "parent:      "),
	/** The &lt;dependencies&gt; tag */
	DEPENDENCIES("dependencies", 1, "dependency:  "),
	/** The &lt;dependencyManagement&gt; tag */
	DEPENDENCY_MANAGEMENT("dependencyManagement", 2, "managed: ");

	/** The tag name for &lt;groupId&gt; */
	public static final String TAG_GROUP_ID = "groupId";
	/** The tag name for &lt;artifactId&gt; */
	public static final String TAG_ARTIFACT_ID = "artifactId";
	/** The tag name for &lt;version&gt; */
	public static final String TAG_VERSION = "version";

	/**
	 * Get a PomTag enum for the specified tag name.
	 *
	 * @param tagName the tag name
	 * @return PomTags the matching enum
	 */
	public static PomTags fromTagName(String tagName) {
		for (PomTags tag : PomTags.values()) {
			if (tag.getTagName().equalsIgnoreCase(tagName)) {
				return tag;
			}
		}
		return null;
	}

	private String tagName;
	private int outputTabs;
	private String outputPrefix;

	/**
	 * Create a PomTag reference.
	 *
	 * @param tagName the pom.xml base tag name
	 * @param tabs the number
	 * @param outputPrefix
	 */
	PomTags(String tagName, int outputTabs, String outputPrefix) {
		this.tagName = tagName;
		this.outputTabs = outputTabs;
		this.outputPrefix = outputPrefix;
	}

	/**
	 * Get the tag name for the tag.
	 *
	 * @return String - the tag name
	 */
	public String getTagName() {
		return this.tagName;
	}

	/**
	 * Get the number of tabs to include in the console output.
	 *
	 * @return int - the number of tabs to use
	 */
	public int getOutputTabs() {
		return this.outputTabs;
	}

	/**
	 * Get the text prefix for the row data to use in the console output.
	 *
	 * @return String - the prefix for the row
	 */
	public String getOutputPrefix() {
		return this.outputPrefix;
	}
}
