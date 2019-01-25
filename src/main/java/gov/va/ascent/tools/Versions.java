package gov.va.ascent.tools;

import java.io.IOException;
import java.io.InputStream;
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
import gov.va.ascent.tools.versions.Reporter;
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

	/** Name of the properties file on the classpath - backlash required */
	private static final String PROPERTIES_FILENAME = "/versions.properties";
	/** Property name for the nexus project base url */
	private static final String PROPS_NEXUS = "versions.nexus.base-projects-url";
	/** Property name for any second level projects to be processed */
	private static final String PROPS_2NDLEVEL = "versions.projects.second-level";

	/** The path to the git directory */
	private String gitHomePath;

	/** The base Nexus URL for Ascent and VetServices projects */
	String nexusUrl;
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
//		gov.va.ascent.tools.utils.SystemUtils.printSystemProperties();
		new Versions().runMe();
	}

	/**
	 * Entry point for running the program in the instantiated Versions object.
	 *
	 * @throws IOException
	 */
	private void runMe() {
		readGitHomePath();
		readProperties();
		versions = new PomVersionsParser(this.versions, this.extraProjects, this.gitHomePath, this.nexusUrl)
				.processProjectDirectories();
		Reporter.buildReport(this.versions, this.gitHomePath);
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
		// using getResourceAsStream instead of Files.newInputStream
		// because Files.newInputStream does not instantiate a ZipFileSystemProvider to read from inside a JAR
		try (InputStream in = this.getClass().getResourceAsStream(PROPERTIES_FILENAME)) {
			props.load(in);
		} catch (IOException e) {
			throw new RuntimeException("While reading versions.properties", e);
		}

		nexusUrl = props.getProperty(PROPS_NEXUS).trim();
		if (StringUtils.isBlank(nexusUrl)) {
			throw new RuntimeException("Cannot have empty " + PROPS_NEXUS + " property in versions.properties.");
		}

		String secondLevel = props.getProperty(PROPS_2NDLEVEL).trim();
		if (!StringUtils.isBlank(secondLevel)) {
			String[] chunks = secondLevel.split(",");
			if (chunks != null) {
				for (String chunk : chunks) {
					chunk = chunk.trim();
					String[] projectChunks = chunk.split("\\[");
					String baseProject = projectChunks[0].trim();
					String[] subProjects = projectChunks[1].trim().replaceAll("]", "").split("\\|");
					for (int i = 0; i < subProjects.length; i++) {
						subProjects[i] = subProjects[i].trim();
					}

					extraProjects.put(baseProject, Arrays.asList(subProjects));
				}
			}
		}
	}
}
