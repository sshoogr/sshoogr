package com.aestasit.ssh

class QuotingUtil {

	private QuotingUtil() { /* No instantiation */ }

	private static final char[] SAFE_CHARACTERS = Collections.unmodifiableSet("-+/_^,.=".toCharArray() as Set<Character>)

	/**
	 * Quote a string as a single UNIX argument
	 *
	 * @param str The string to be quoted
	 * @return A quoted variant of the input string that will be exactly one command line
	 * argument (which in some cases may be identical with the input string).
	 */
	public static String quoteUNIXArgument(String str) {
		boolean needsQoute = false
		StringBuilder quotedStr
		int last
		int strLength = str.length()
		int start = 0
		for(int i = 0 ; i < strLength ; i++) {
			char c = str.charAt(i)
			if (!(Character.isLetterOrDigit(c) || c in SAFE_CHARACTERS)) {
				needsQoute = true
				break
			}
		}
		if (!needsQoute) {
			return str
		}
		/* handle two special cases */
		if (str == "'") {
			return "\\'"
		}
		if (str == "''") {
			return "\\'\\'"
		}

		quotedStr = new StringBuilder(strLength + 10)

		/* If it starts with a quote, escape that and start a quote otherwise start a quote */
		if (strLength > 0 && str.charAt(0) == "'") {
			start++
			quotedStr.append("\\''")
		} else {
			quotedStr.append("'")
		}
		for(int i = start ; i < strLength ; i++) {
			char c = str.charAt(i)
			if (c == "'") {
				quotedStr.append("'\\''")
			} else {
				quotedStr.append(c)
			}
		}
		last = quotedStr.length() - 1
		/*
		 * If the string ends with a quote, drop the trailing ' quote.
		 * - In this case quoted will end with "...'\''" and we want it to be "...'\'".
		 */
		if (strLength > 1 && quotedStr.charAt(last) == "'") {
			quotedStr.setLength(last)
		} else {
			quotedStr.append("'")
		}
		return quotedStr.toString()
	}

}
