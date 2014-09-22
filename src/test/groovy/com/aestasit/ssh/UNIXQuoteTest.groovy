package com.aestasit.ssh;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aestasit.ssh.dsl.ExecMethods;

class UNIXQuoteTest {

	List<Map<String, String>> TESTS = [
		addTest('a', 'a'),
		addTest('a b', "'a b'"),
		addTest("'", "\\'"),
		addTest("''", "\\'\\'"),
		addTest("'a", "\\''a'"),
		addTest("a'", "'a'\\'"),
		addTest("'a'", "\\''a'\\'"),
		addTest("Don't", "'Don'\\''t'"),
		addTest("Stop!", "'Stop!'"),
		addTest('$#@!', '\'$#@!\''),
		addTest("\\'\\x23", "'\\'\\''\\x23'"),
		addTest("'a'b'c'", "\\''a'\\''b'\\''c'\\'"),
		addTest('-e', '-e'),
	]

	@Test
	public void test() {
		for (def test : TESTS) {
			assertEquals(test.testMessage, test.expected, QuotingUtil.quoteUNIXArgument(test.input))
		}
	}


	private Map<String, String> addTest(String input, String expected, String testMessage = null) {
		testMessage = testMessage ? testMessage : "Quote ${input} => ${expected}"
		return [
			input: input,
			expected: expected,
			testMessage: testMessage,
		]
	}
}
