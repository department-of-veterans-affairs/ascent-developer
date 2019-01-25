package gov.va.ascent.tools.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class SystemUtils {

	public SystemUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Print all {@code System.getenv()} variables to the console.
	 */
	public static void printEnv() {
		Map<String, String> env = System.getenv();
		int longestKey = 0;
		for (Entry<String, String> entry : env.entrySet()) {
			longestKey = Math.max(longestKey, entry.getKey().length());
		}
		for (Entry<String, String> entry : env.entrySet()) {
			Out.println(entry.getKey()
					+ StringUtils.repeat(" ", longestKey - entry.getKey().length())
					+ " = "
					+ entry.getValue());
		}
	}

	/**
	 * Print all {@code Ssystem.getProperties()} variables to the console.
	 */
	public static void printSystemProperties() {
		Properties props = System.getProperties();

		int longestKey = 0;
		for (Entry<Object, Object> entry : props.entrySet()) {
			longestKey = Math.max(longestKey, ((String) entry.getKey()).length());
		}
		for (Entry<Object, Object> entry : props.entrySet()) {
			Out.println(entry.getKey()
					+ StringUtils.repeat(" ", longestKey - ((String) entry.getKey()).length())
					+ " = "
					+ entry.getValue());
		}
	}

}
