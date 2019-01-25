package gov.va.ascent.tools.versions;

import org.apache.commons.lang3.StringUtils;

import gov.va.ascent.tools.versions.model.Revision;

/**
 * Parses a maven &lt;version&gt; string into its component {@link Revision} information.
 *
 * @author aburkholder
 */
public class RevisionParser {

	/** Constant for SNAPSHOT revision string */
	private static final String SNAPSHOT = "-SNAPSHOT";

	/**
	 * Don't instantiate
	 */
	private RevisionParser() {
		throw new IllegalAccessError("RevisionParser is static. Do not instantiate it.");
	}

	/**
	 * Parse a maven &lt;version&gt; string into its component {@link Revision} information.
	 * <p>
	 * The standard maven &lt;version&gt; format is: {@code #.#.#[-SNAPSHOT]}
	 * where each # is an integer, with optional snapshot indicator.
	 * <p>
	 * If any part of the version parameter does not conform to the standard &lt;version&gt; format,
	 * {@code null} is returned.
	 *
	 * @param version - the maven &lt;version&gt; string
	 * @return Revision - the revision information, or {@code null}
	 */
	public static Revision parseRevision(String version) {
		if (StringUtils.isBlank(version)) {
			return null;
		}

		boolean snapshot = version.contains(SNAPSHOT);
		if (snapshot) {
			version = version.replace(SNAPSHOT, "");
		}

		String[] versions = version.split("\\.");
		if (versions == null || versions.length != 3
				|| StringUtils.isBlank(versions[0]) || StringUtils.isBlank(versions[1]) || StringUtils.isBlank(versions[2])) {
			return null;
		}

		int major = 0;
		int minor = 0;
		int build = 0;
		try {
			major = Integer.parseInt(versions[0].trim());
			minor = Integer.parseInt(versions[1].trim());
			build = Integer.parseInt(versions[2].trim());
		} catch (NumberFormatException e) {
			return null;
		}

		return new Revision(major, minor, build, snapshot);
	}
}
