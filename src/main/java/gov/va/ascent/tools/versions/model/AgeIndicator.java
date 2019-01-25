package gov.va.ascent.tools.versions.model;

/**
 * Enumerates the relevant age concerns for versioned artifacts.
 *
 * @author aburkholder
 */
public enum AgeIndicator {
	/** Version is the current SNAPSHOT */
	CURRENT_SNAPSHOT(1, "✓ "),
	/** Version is the current release */
	CURRENT_RELEASE(0, "✓ "),
	/** Version is an old release */
	OLD_RELEASE(-1, "℞ "),
	/** Version is an old snapshot */
	OLD_SNAPSHOT(-2, "﹆ "),
	/** Version is not found in nexus */
	NOT_FOUND(-3, "✗ "),
	/** Not enough information to determine age */
	UNKNOWN(-9, "? ");

	/** Precedence of the age indicator, between 1 and -9 */
	private int precedence;
	/** An indicator string to visually identify the age */
	private String indicator;

	/**
	 * A moldy indicator is one that is OLD_RELEASE, OLD_SNAPSHOT, or NOT_FOUND.
	 *
	 * @param age - the AgeIndicator to check
	 * @return boolean - {@code true} if one of the AgeIndicators mentioned above
	 */
	public static boolean isMoldy(AgeIndicator age) {
		return age.equals(OLD_RELEASE)
				|| age.equals(OLD_SNAPSHOT)
				|| age.equals(NOT_FOUND);
	}

	/**
	 * Instantiate the enumeration.
	 *
	 * @param precedence - an integer betwee 1 and -9
	 * @param indicator - a string identifier
	 */
	AgeIndicator(int precedence, String indicator) {
		this.precedence = precedence;
		this.indicator = indicator;
	}

	/**
	 * A moldy indicator is one that is OLD_RELEASE, OLD_SNAPSHOT, or NOT_FOUND.
	 *
	 * @param age - the AgeIndicator to check
	 * @return boolean - {@code true} if one of the AgeIndicators mentioned above
	 */
	public boolean isMoldy() {
		return AgeIndicator.isMoldy(this);
	}

	/**
	 * Get the numeric precedence of the indicator.
	 *
	 * @return int - the precedence value
	 */
	public int getPrecedence() {
		return this.precedence;
	}

	/**
	 * Get the string identifier.
	 *
	 * @return String - the identifier
	 */
	public String getIndicator() {
		return this.indicator;
	}
}
