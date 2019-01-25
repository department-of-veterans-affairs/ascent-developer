package gov.va.ascent.tools.versions.model;

import gov.va.ascent.tools.versions.RevisionParser;

/**
 * A revision object that decomposes a &lt;version&gt; into its component parts.
 * 
 * @author aburkholder
 */
public class Revision {

	/** Constant for multiplying the major revision number */
	private static final int MULT_MAJOR = 10000;
	/** Constant for multiplying the minor revision number */
	private static final int MULT_MINOR = 100;

	/** the major revision number */
	private int major;
	/** the minor revision number */
	private int minor;
	/** the build revision number */
	private int build;
	/** whether the version is a SNAPSHOT (true) or not (false) */
	private boolean snapshot;

	/**
	 * Decompose a &lt;version&gt; into a {@link Revision} object.
	 * 
	 * @param version - the &lt;version&gt; to decompose
	 * @return Revision - the decomposed &lt;version&gt;
	 */
	public static Revision getRevisionFromVersion(String version) {
		return RevisionParser.parseRevision(version);
	}

	/**
	 * Create a new {@link Revision}.
	 * 
	 * @param major - the major revision number
	 * @param minor - the minor revision number
	 * @param build - the build number
	 * @param snapshot - {@code true} if a SNAPSHOT
	 */
	public Revision(int major, int minor, int build, boolean snapshot) {
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.snapshot = snapshot;
	}

	/**
	 * Get a numeric value indicating the revision level of this object.
	 * This method assumes none of the major, minor, or build numbers will individually exceed 99.
	 * <p>
	 * The returned int will be have 5 or 6 digits. Working from right to left:
	 * <ul>
	 * <li>the first two right-most digits are the build number
	 * <li>the next two digits (in the center) are the minor number
	 * <li>the left-most digit or two digits are the major number
	 * </ul>
	 * Examples ("-SNAPSHOT" applies if .isSnapshot() == {@code true}) :
	 * <ul>
	 * <li>return value of {@code 110215} is &lt;version&gt;11.02.15&lt;/version&gt;
	 * <li>return value of {@code 10104} is &lt;version&gt;01.01.04&lt;/version&gt;
	 * <li>return value of {@code 3} is &lt;version&gt;00.00.03&lt;/version&gt;
	 * </ul>
	 *
	 * @return int - the numeric rendition of the revisions
	 */
	public int getRevisionValue() {
		return this.major * MULT_MAJOR + this.minor * MULT_MINOR + this.build;
	}

	/**
	 * Compares another {@link Revision} object to this.
	 * <p>
	 * The comparison assumes that this object is the current, or newest Revision.
	 * <p>
	 * If the otherRevision is newer than this one (including consideration of SNAPSHOTs), the value returned
	 * will always be {@link AgeIndicator#CURRENT_RELEASE} or {@link AgeIndicator#CURRENT_SNAPSHOT}.
	 * <p>
	 * It is assumed that there is always a SNAPSHOT version that is exactly one build revision greater than the most recent release.
	 * Therefore:
	 * <ul>
	 * <li>If both Revision objects have the same number revisions, but one is a snapshot, the snapshot is considered to be older.
	 * <li>
	 * </ul>
	 *
	 * @param otherRevision
	 * @return
	 */
	public AgeIndicator compare(Revision otherRevision) {
		if (otherRevision == null) {

		}

		int thisValue = this.major * MULT_MAJOR + this.minor * MULT_MINOR + this.build;
		int otherValue = otherRevision.getMajor() * MULT_MAJOR + otherRevision.getMinor() * MULT_MINOR + this.build;

		// check if otherRevision is newer
		if (thisValue < otherValue) {
			if (otherRevision.isSnapshot()) {
				return AgeIndicator.CURRENT_SNAPSHOT;
			} else {
				return AgeIndicator.CURRENT_RELEASE;
			}
		}
		if (this.isSnapshot() && thisValue == otherValue && !otherRevision.isSnapshot()) {
			return AgeIndicator.CURRENT_RELEASE;
		}

		// check if otherRevision is current
		if (thisValue == otherValue) {
			if (otherRevision.isSnapshot() && this.isSnapshot()) {
				return AgeIndicator.CURRENT_SNAPSHOT;
			} else if (otherRevision.isSnapshot()) {
				return AgeIndicator.OLD_SNAPSHOT;
			} else if (this.isSnapshot()) { // should never get this, as it is handled above
				return AgeIndicator.CURRENT_RELEASE;
			} else { // neither are snapshots
				return AgeIndicator.CURRENT_RELEASE;
			}
		}

		// check how old otherRevision is
		if (thisValue > otherValue) {
			if (otherRevision.isSnapshot() && this.isSnapshot()) {
				return AgeIndicator.OLD_SNAPSHOT;
			} else if (!otherRevision.isSnapshot() && !this.isSnapshot()) {
				return AgeIndicator.OLD_RELEASE;
			} else if (this.isSnapshot()) { // otherRevision is not a snapshot
				if (thisValue - otherValue == 1) {
					return AgeIndicator.CURRENT_RELEASE;
				} else {
					return AgeIndicator.OLD_RELEASE;
				}
			} else { // otherRevision is a snapshot, this is not
				return AgeIndicator.OLD_SNAPSHOT;
			}
		}

		return null;
	}

	/**
	 * The Major revision number (1st position in the version).
	 *
	 * @return major - the 1st position (major) number
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * The Major revision number.
	 *
	 * @param major - the 1st position (major) number
	 */
	public void setMajor(int major) {
		this.major = major;
	}

	/**
	 * The Minor revision number (2nd position in the version).
	 *
	 * @return minor - the 2nd position (minor) number
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * The Minor revision number (2nd position in the version).
	 *
	 * @param minor - the 2nd position (minor) number
	 */
	public void setMinor(int minor) {
		this.minor = minor;
	}

	/**
	 * The Build revision number (3rd position in the version).
	 *
	 * @return build - the 3rd position (build) number
	 */
	public int getBuild() {
		return build;
	}

	/**
	 * The Build revision number (3rd position in the version).
	 *
	 * @param build - the 3rd position (build) number
	 */
	public void setBuild(int build) {
		this.build = build;
	}

	/**
	 * If a SNAPSHOT, {@code true}.
	 *
	 * @return snapshot - {@code true} if a SNAPSHOT
	 */
	public boolean isSnapshot() {
		return snapshot;
	}

	/**
	 * If a SNAPSHOT, {@code true}.
	 *
	 * @param snapshot {@code true} if it is a SNAPSHOT
	 */
	public void setSnapshot(boolean snapshot) {
		this.snapshot = snapshot;
	}

}
