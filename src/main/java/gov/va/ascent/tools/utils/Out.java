package gov.va.ascent.tools.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for console interactions.
 *
 * @author aburkholder
 */
public class Out {

	private static final int TAB_LEN = 4;

	private Out() {
		throw new IllegalAccessError("Out is a static class. Do not instantiate it.");
	}

	/**
	 * Print a line with carriage return.
	 *
	 * @param message the message to print
	 */
	public static void println(String message) {
		println(0, null, message, null);
	}

	public static void println(int tabs, String message) {
		println(tabs, null, message, null);
	}

	public static void println(int tabs, Severity severity, String message) {
		println(tabs, severity, message, null);
	}

	public static void println(int tabs, Severity severity, String message, Throwable t) {
		System.out.println(
				(tabs < 1 ? "" : StringUtils.repeat(" ", tabs * TAB_LEN))
						+ (severity == null ? "" : severity.toString() + ": ")
						+ (message == null ? "" : message));
		if (t != null) {
			t.printStackTrace();
		}
	}

	public static void printlns(List<String> messages) {
		printlns(0, null, messages);
	}

	public static void printlns(int tabs, List<String> messages) {
		printlns(tabs, null, messages);
	}

	public static void printlns(int tabs, Severity severity, List<String> messages) {
		if (messages != null && messages.size() > 0) {
			for (String message : messages) {
				System.out.println(
						(tabs < 1 ? "" : StringUtils.repeat(" ", tabs * TAB_LEN))
								+ (severity == null ? "" : severity.toString() + ": ")
								+ (message == null ? "" : message));
			}
		}
	}

	public static void print(String message) {
		print(0, null, message);
	}

	public static void print(int tabs, String message) {
		print(tabs, null, message);
	}

	public static void print(int tabs, Severity severity, String message) {
		System.out.print(
				(tabs < 1 ? "" : StringUtils.repeat(" ", tabs * TAB_LEN))
						+ (severity == null ? "" : severity.toString() + ": ")
						+ (message == null ? "" : message));
	}
}
