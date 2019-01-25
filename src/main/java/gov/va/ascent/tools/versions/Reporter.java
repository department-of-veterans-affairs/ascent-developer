package gov.va.ascent.tools.versions;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import gov.va.ascent.tools.utils.Out;
import gov.va.ascent.tools.versions.model.AgeIndicator;
import gov.va.ascent.tools.versions.model.Version;

/**
 * Generate the Versions report.
 *
 * @author aburkholder
 */
public class Reporter {

	/** Constant for space character */
	private static final String SPACE = " ";
	/** Constant for desired max line length */
	private static final int LINELEN = 79;

	/** The map keyed by project path and the related Version object */
	private Map<String, Version> versions;

	/**
	 * Invoked only by the static buildReport(...) method, otherwise potential thread crossover issues.
	 */
	private Reporter(Map<String, Version> versions) {
		this.versions = versions;
	}

	/**
	 * Build the report, print it to standard out, and return an equivalent string of the return.
	 *
	 * @param versions - the map of {@link Version} objects to report from
	 * @param gitHomePath - the value of the GIT_HOME env var
	 * @return String - a string representation of the report printed to console
	 */
	public static void buildReport(Map<String, Version> versions, String gitHomePath) {
		new Reporter(versions).printReport(gitHomePath);
	}

	/**
	 * Print a report of version information to the console.
	 */
	private void printReport(String gitHomePath) {
		Out.println("");
		Out.println(StringUtils.repeat("=", LINELEN));
		Out.println("Version Report for " + gitHomePath + "/**");
		Out.println("Legend:  " + AgeIndicator.CURRENT_SNAPSHOT.getIndicator() + " current snapshot found in nexus");
		Out.println("         " + AgeIndicator.CURRENT_RELEASE.getIndicator() + " current release found in nexus");
		Out.println("         " + AgeIndicator.OLD_SNAPSHOT.getIndicator()
				+ "  old snapshot may soon be deleted from nexus");
		Out.println("         " + AgeIndicator.OLD_RELEASE.getIndicator() + "  newer release available");
		Out.println("         " + AgeIndicator.NOT_FOUND.getIndicator() + "  not found in nexus");
		Out.println("         " + AgeIndicator.UNKNOWN.getIndicator() + "  not enough info to determine age");
		Out.println("");

		versions.forEach(this::printVersion);
		Out.println("");
		Out.println("EOF");
	}

	/**
	 * Print report rows of the collected version info for a specific project.
	 *
	 * @param projectPath - disc path to the project
	 * @param rootElement - the {@link Version} object representing the &lt;project&gt; element
	 */
	private void printVersion(String projectPath, Version rootElement) {
		Out.println("");
		Out.println(
				"---- " + projectPath.toString() + " " + StringUtils.repeat("-", LINELEN - 6 - projectPath.toString().length()));

		if (rootElement != null) {

			Out.println(nexusIndicator(rootElement, false) + rootElement.getArtifactId() + SPACE + rootElement.getVersion());

			if (rootElement.getParent() != null) {
				Version parent = rootElement.getParent();
				Version currentVersion = findCurrentVersion(parent);
				AgeIndicator age =
						currentVersion == null ? AgeIndicator.UNKNOWN : currentVersion.getRevision().compare(parent.getRevision());
				Out.println(age.getIndicator(), 1,
						(parent.getHierarchyIdTag() == null ? "" : parent.getHierarchyIdTag().getOutputPrefix())
								+ parent.getArtifactId()
								+ SPACE + parent.getVersion());
				if (age.isMoldy()) {
					Out.println("  ", 4, "▷ Current version is: " + currentVersion);
				}
			}

			if (rootElement.getDependencies() != null) {
				for (Version dep : rootElement.getDependencies()) {
					if (dep != null) {
						Version currentVersion = findCurrentVersion(dep);
						AgeIndicator age = currentVersion == null ? AgeIndicator.UNKNOWN
								: currentVersion.getRevision().compare(dep.getRevision());
						Out.println(age.getIndicator(), 1,
								(dep.getHierarchyIdTag() == null ? "" : dep.getHierarchyIdTag().getOutputPrefix())
										+ dep.getArtifactId() + SPACE + dep.getVersion());
						if (age.isMoldy()) {
							Out.println(3, "▷ Current version is: " + currentVersion);
						}
					}
				}
			}
		}
	}

	/**
	 * Searches the version map for the artifact related to the dependency or parent element.
	 * <p>
	 * If the artifact is not found, it is assumed to be a third party artifact (spring or whatever),
	 * and {@code null} is returned.
	 *
	 * @param element - the dependency or parent element
	 * @return
	 */
	private Version findCurrentVersion(Version element) {
		Version current = null;
		for (Version version : versions.values()) {
			if (version != null && version.getArtifactId() != null
					&& version.getArtifactId().equals(element.getArtifactId())) {
				current = version;
				break;
			}
		}
		return current == null ? null : current;
	}

	/**
	 * Get the string that represents whether the artifact version exists in nexus.
	 *
	 * @param element - the {@link Version} object representing the nexus artifact.
	 * @return String - the indicator string
	 */
	private String nexusIndicator(Version element, boolean isMoldy) {
		Boolean exists = element.exists();

		String ret = null;
		if (Boolean.FALSE.equals(exists)) {
			ret = "✗  ";
		} else if (Boolean.TRUE.equals(exists)) {
			if (isMoldy) {
				ret = "℞︎ ";
			} else {
				ret = "✓︎ ";
			}
		} else {
			ret = "  ";
		}
		return ret;
	}
}
