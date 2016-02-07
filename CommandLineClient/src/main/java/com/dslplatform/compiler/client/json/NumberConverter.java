package com.dslplatform.compiler.client.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

abstract class NumberConverter {

	private final static double[] POW_10 = new double[18];

	static {
		long tenPow = 1;
		for (int i = 0; i < POW_10.length; i++) {
			POW_10[i] = tenPow;
			tenPow = tenPow * 10;
		}
	}

	private static BigDecimal parseNumberGeneric(final char[] buf, final int len, final int position) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		try {
			return new BigDecimal(buf, 0, end);
		} catch (NumberFormatException nfe) {
			throw new IOException("Error parsing number at position: " + (position - len), nfe);
		}
	}

	private static class NumberInfo {
		public final char[] buffer;
		public final int length;

		public NumberInfo(final char[] buffer, final int length) {
			this.buffer = buffer;
			this.length = length;
		}
	}

	private static NumberInfo readLongNumber(final JsonReader reader, final char[] buf) throws IOException {
		int i = buf.length;
		char[] tmp = Arrays.copyOf(buf, buf.length * 2);
		while (!reader.isEndOfStream()) {
			do {
				final char ch = (char) reader.read();
				tmp[i++] = ch;
				if (reader.isEndOfStream() || !(ch >= '0' && ch < '9' || ch == '-' || ch == '+' || ch == '.' || ch == 'e' || ch == 'E')) {
					return new NumberInfo(tmp, reader.isEndOfStream() ? i : i - 1);
				}
			} while (i < tmp.length);
			tmp = Arrays.copyOf(tmp, tmp.length * 2);
		}
		return new NumberInfo(tmp, i);
	}

	private static int parsePositiveInt(final char[] buf, final int position, final int len, int i) throws IOException {
		int value = 0;
		for (; i < len; i++) {
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				BigDecimal v = parseNumberGeneric(buf, len, position);
				if (v.scale() <= 0) return v.intValue();
				throw new IOException("Error parsing int number at position: " + (position - len) + ". Found decimal value: " + v);
			}
		}
		return value;
	}

	private static int parseNegativeInt(final char[] buf, final int position, final int len, int i) throws IOException {
		int value = 0;
		for (; i < len; i++) {
			final int ind = buf[i] - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				BigDecimal v = parseNumberGeneric(buf, len, position);
				if (v.scale() <= 0) return v.intValue();
				throw new IOException("Error parsing int number at position: " + (position - len) + ". Found decimal value: " + v);
			}
		}
		return value;
	}

	public static Number deserializeNumber(final JsonReader reader) throws IOException {
		final char[] buf = reader.readNumber();
		final int position = reader.getCurrentIndex();
		final int len = position - reader.getTokenStart();
		if (len > 18) {
			if (len == buf.length) {
				final NumberInfo tmp = readLongNumber(reader, buf);
				return parseNumberGeneric(tmp.buffer, tmp.length, position);
			} else {
				return parseNumberGeneric(buf, len, position);
			}
		}
		final char ch = buf[0];
		if (ch == '-') {
			return parseNegativeNumber(buf, position, len);
		} else if (ch == '+') {
			return parsePositiveNumber(buf, position, len, 1);
		}
		return parsePositiveNumber(buf, position, len, 0);
	}

	private static Number parsePositiveNumber(final char[] buf, final int position, final int len, int i) throws IOException {
		long value = 0;
		char ch = ' ';
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			value = (value << 3) + (value << 1) + ind;
			if (ind < 0 || ind > 9) {
				return parseNumberGeneric(buf, len, position);
			}
		}
		if (i == len) return value;
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < len; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) + ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(buf, len, position);
				}
			}
			if (i == len) return value / POW_10[len - dp];
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, position, len, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, position, len, i + 1);
				} else {
					exp = parsePositiveInt(buf, position, len, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, len - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, position, len, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, position, len, i + 1);
			} else {
				exp = parsePositiveInt(buf, position, len, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static Number parseNegativeNumber(final char[] buf, final int position, final int len) throws IOException {
		long value = 0;
		char ch = ' ';
		int i = 1;
		for (; i < len; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			value = (value << 3) + (value << 1) - ind;
			if (ind < 0 || ind > 9) {
				return parseNumberGeneric(buf, len, position);
			}
		}
		if (i == len) return value;
		else if (ch == '.') {
			i++;
			int dp = i;
			for (; i < len; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				value = (value << 3) + (value << 1) - ind;
				if (ind < 0 || ind > 9) {
					return parseNumberGeneric(buf, len, position);
				}
			}
			if (i == len) return value / POW_10[len - dp];
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, position, len, i + 1);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, position, len, i + 1);
				} else {
					exp = parsePositiveInt(buf, position, len, i);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, len - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, position, len, i + 1);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, position, len, i + 1);
			} else {
				exp = parsePositiveInt(buf, position, len, i);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}
}