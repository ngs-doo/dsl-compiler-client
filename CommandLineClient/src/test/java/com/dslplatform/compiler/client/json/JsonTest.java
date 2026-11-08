package com.dslplatform.compiler.client.json;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class JsonTest {

	private static byte[] utf8(final String json) {
		return json.getBytes(StandardCharsets.UTF_8);
	}

	private static Object readMapValue(final String json, final String key) throws IOException {
		final byte[] input = utf8(json);
		return JSON.readMap(input, input.length).get(key);
	}

	private static Object readListItem(final String json, final int index) throws IOException {
		final byte[] input = utf8(json);
		return JSON.readList(input, input.length).get(index);
	}

	@Test
	public void stringsAreDecodedAsUtf8() throws IOException {
		assertEquals("caf\u00e9", readMapValue("{\"a\":\"caf\u00e9\"}", "a"));
		final StringBuilder longString = new StringBuilder();
		for (int i = 0; i < 30; i++) {
			longString.append("word\u00e9 ");
		}
		assertEquals(longString.toString(), readMapValue("{\"a\":\"" + longString + "\"}", "a"));
		assertEquals("caf\u00e9\nx", readMapValue("{\"a\":\"caf\u00e9\\nx\"}", "a"));
		assertEquals("\u00e9", readMapValue("{\"a\":\"\\u00e9\"}", "a"));
	}

	@Test
	public void fourByteUtf8CharactersAreDecoded() throws IOException {
		assertEquals("\ud83d\ude00", readMapValue("{\"a\":\"\ud83d\ude00\"}", "a"));
		final StringBuilder many = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			many.append("\ud83d\ude00");
		}
		assertEquals(many.toString(), readMapValue("{\"a\":\"" + many + "\"}", "a"));
	}

	@Test
	public void numbersAreParsed() throws IOException {
		assertEquals(1L, readListItem("[1]", 0));
		assertEquals(-2L, readListItem("[-2]", 0));
		assertEquals(3.14, readListItem("[3.14]", 0));
		assertEquals(new BigDecimal("1E+5"), readListItem("[1e5]", 0));
		assertEquals(new BigDecimal("0.015"), readListItem("[1.5e-2]", 0));
		assertEquals(-0.5, readListItem("[-0.5]", 0));
		assertEquals(999999999999999999L, readListItem("[999999999999999999]", 0));
		assertEquals(new BigDecimal("18446744073709551615"), readListItem("[18446744073709551615]", 0));
	}

	@Test
	public void invalidNumbersAreRejected() {
		assertNumberError("[1e]");
		assertNumberError("[-]");
		assertNumberError("[1.]");
		assertNumberError("[.5]");
		assertNumberError("[\"abcdef\",1e]");
	}

	private static void assertNumberError(final String json) {
		try {
			final byte[] input = utf8(json);
			if (json.startsWith("[")) {
				JSON.readList(input, input.length);
			} else {
				JSON.readMap(input, input.length);
			}
			fail("Expecting error for: " + json);
		} catch (final IOException expected) {
		}
	}

	@Test
	public void truncatedStringDoesNotReadPastLength() {
		final byte[] buffer = new byte[64];
		final String json = "{\"a\":\"xy\"";
		final byte[] raw = json.getBytes(StandardCharsets.UTF_8);
		System.arraycopy(raw, 0, buffer, 0, raw.length);
		for (int i = raw.length; i < buffer.length; i++) {
			buffer[i] = (byte) 'Z';
		}
		buffer[raw.length + 5] = '"'; // stale quote beyond logical end of the input
		try {
			JSON.readMap(buffer, raw.length);
			fail("Expecting error for truncated string");
		} catch (final IOException expected) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void structureIsPreserved() throws IOException {
		assertEquals(null, readMapValue("{\"a\":null}", "a"));
		assertEquals(Boolean.TRUE, readMapValue("{\"a\":true}", "a"));
		assertEquals(Boolean.FALSE, readMapValue("{\"a\":false}", "a"));
		final List<Object> nested = (List<Object>) readListItem("[[1,\"a\"]]", 0);
		assertEquals(2, nested.size());
	}
}
