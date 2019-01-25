package gov.va.ascent.tools.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
	public static String println(String message) {
		return println(null, 0, null, message, null);
	}

	public static String println(int tabs, String message) {
		return println(null, tabs, null, message, null);
	}

	public static String println(int tabs, Severity severity, String message) {
		return println(null, tabs, severity, message, null);
	}

	public static String println(String indicator, int tabs, String message) {
		return println(indicator, tabs, null, message, null);
	}

	public static String println(String indicator, int tabs, Severity severity, String message) {
		return println(indicator, tabs, severity, message, null);
	}

	public static String println(int tabs, Severity severity, String message, Throwable t) {
		return println(null, tabs, severity, message, t);
	}

	public static String println(String indicator, int tabs, Severity severity, String message, Throwable t) {
		String throwable = t == null ? null : ExceptionUtils.getStackTrace(t);
		String output = (indicator == null ? "" : indicator)
				+ (tabs < 1 ? "" : StringUtils.repeat(" ", tabs * TAB_LEN))
				+ (severity == null ? "" : severity.toString() + ": ")
				+ (message == null ? "" : message)
				+ (throwable == null ? "" : "\\n" + throwable);

		System.out.println(output);
		return output;
	}

	public static String printlns(List<String> messages) {
		return printlns(0, null, messages);
	}

	public static String printlns(int tabs, List<String> messages) {
		return printlns(tabs, null, messages);
	}

	public static String printlns(int tabs, Severity severity, List<String> messages) {
		String outputs = "";
		if (messages != null && messages.size() > 0) {
			for (String message : messages) {
				String output = (tabs < 1 ? "" : StringUtils.repeat(" ", tabs * TAB_LEN))
						+ (severity == null ? "" : severity.toString() + ": ")
						+ (message == null ? "" : message);

				outputs += (outputs.isEmpty() ? "" : "\\n") + output;
				System.out.println(output);
			}
		}
		return outputs;
	}

	public static String print(String message) {
		return print(null, 0, null, message);
	}

	public static String print(int tabs, String message) {
		return print(null, tabs, null, message);
	}

	public static String print(int tabs, Severity severity, String message) {
		return print(null, tabs, null, message);
	}

	public static String print(String indicator, int tabs, Severity severity, String message) {
		String output =
				(indicator == null ? "" : indicator)
						+ (tabs < 1 ? "" : StringUtils.repeat(" ", tabs * TAB_LEN))
						+ (severity == null ? "" : severity.toString() + ": ")
						+ (message == null ? "" : message);

		System.out.print(output);
		return output;
	}
}
