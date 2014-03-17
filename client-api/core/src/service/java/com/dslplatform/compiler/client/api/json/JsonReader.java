package com.dslplatform.compiler.client.api.json;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonReader {
    private final Reader reader;

    public JsonReader(final Reader reader) {
        this.reader = reader;
    }

    private boolean _endOfStream;
    private char _last;
    private boolean _lastValid;

    public char next() throws IOException {
        if (_endOfStream) throw new IOException("Could not read past the end of stream");

        final int next = reader.read();
        if (next == -1) {
            _lastValid = false;
            _endOfStream = true;
            throw new IOException("Unexpected end of input stream");
        }

        _lastValid = true;
        return _last = (char) next;
    }

    public int peek() throws IOException {
        if (_endOfStream) throw new IOException("Could not peek past the end of stream");

        final int peek = reader.read();

        if (peek == -1) {
            _lastValid = false;
            _endOfStream = true;
        }
        else {
            _lastValid = true;
            _last = (char) peek;
        }
        return peek;
    }

    public char last() throws IOException {
        if (!_lastValid) {
            if (_endOfStream)
                throw new IOException("Could not reuse last() character because the stream has ended!");
            throw new IOException("Could not reuse last() character because it is not valid; use read() or next() instead!");
        }
        return _last;
    }

    public char read() throws IOException {
        return _lastValid ? _last : next();
    }

    public void invalidate() throws IOException {
        _lastValid = false;
    }

    public void assertNext(final char expected) throws IOException {
        if (next() != expected) throw new IOException("Could not parse token, expected '" + expected + "', got '" + last() + "'");
        invalidate();
    }

    public void assertLast(final char expected) throws IOException {
        if (last() != expected) throw new IOException("Could not parse token, expected '" + expected + "', got '" + last() + "'");
        invalidate();
    }

    public void assertRead(final char expected) throws IOException {
        if (_lastValid) assertLast(expected); else assertNext(expected);
    }

    public int nextHexDigit() throws IOException {
        final char next = next();
        if (next >= '0' && next <= '9') return next - 0x30;
        if (next >= 'A' && next <= 'F') return next - 0x37;
        if (next >= 'a' && next <= 'f') return next - 0x57;
        throw new IOException("Could not parse unicode escape, expected a hexadecimal digit, got '" + next + "'");
    }

    public <T> T readNull() throws IOException {
        if (read() == 'n' && next() == 'u' && next() == 'l' && next() == 'l') {
            invalidate();
            return (T) null;
        }

        throw new IOException("Could not parse token, expected 'null'");
    }

    public boolean readTrue() throws IOException {
        if (read() == 't' && next() == 'r' && next() == 'u' && next() == 'e') {
            invalidate();
            return true;
        }

        throw new IOException("Could not parse token, expected 'true'");
    }

    public boolean readFalse() throws IOException {
        if (read() == 'f' && next() == 'a' && next() == 'l' && next() == 's' && next() == 'e') {
            invalidate();
            return false;
        }

        throw new IOException("Could not parse token, expected 'false'");
    }

    public StringBuilder readRawNumber(final StringBuilder sb) throws IOException {
        char ch = read();
        if (ch == '-') {
            sb.append(ch);
            ch = next();
        }

        final int length = sb.length();
        if (ch == '0') {
            sb.append(ch);
            final int chp = peek();
            if (chp == -1) return sb;
            ch = (char) chp;
        } else if (ch >= '1' && ch <= '9') {
            sb.append(ch);
            for (;;) {
                final int chp = peek();
                if (chp == -1) return sb;

                ch = (char) chp;
                if (ch < '0' || ch > '9') break;
                sb.append(ch);
            }
        }

        if (ch == '.') {
            sb.append(ch);
            ch = next();

            if (ch < '0' || ch > '9') throw new IOException("Expected decimal after floating point, got: " + ch);

            sb.append(ch);
            for (;;) {
                final int chp = peek();
                if (chp == -1) return sb;

                ch = (char) chp;
                if (ch < '0' || ch > '9') break;
                sb.append(ch);
            }
        }

        if (ch == 'e' || ch == 'E') {
            sb.append(ch);
            ch = next();

            if (ch == '-' || ch == '+') {
                sb.append(ch);
                ch = next();
            }

            if (ch < '0' || ch > '9') throw new IOException("Expected decimal after exponent sign, got: " + ch);

            sb.append(ch);
            for (;;) {
                final int chp = peek();
                if (chp == -1) return sb;

                ch = (char) chp;
                if (ch < '0' || ch > '9') break;
                sb.append(ch);
            }
        }

        if (sb.length() == length)
            throw new IOException("Could not parse number - no leading digits found!");

        return sb;
    }

    public String readString() throws IOException {
        if (read() != '"')
            throw new IOException("Could not parse String, expected '\"', got '" + last() + "'");

        final StringBuilder sb = new StringBuilder();
        while (next() != '"') {
            if (last() == '\\') {
                final char ch = next();
                switch (ch) {
                    case 'b': sb.append('\b'); continue;
                    case 't': sb.append('\t'); continue;
                    case 'n': sb.append('\n'); continue;
                    case 'f': sb.append('\f'); continue;
                    case 'r': sb.append('\r'); continue;
                    case '"':
                    case '/':
                    case '\\': sb.append(ch); continue;
                    case 'u': sb.append(
                            (nextHexDigit() << 12) |
                            (nextHexDigit() <<  8) |
                            (nextHexDigit() <<  4) |
                            nextHexDigit()); continue;
                }

                throw new IOException("Could not parse String, got invalid escape combination '\\" + ch + "'");
            }

            sb.append(last());
        }

        invalidate();
        return sb.toString();
    }

    public Map<String, Object> readMap() throws IOException {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();

        assertRead('{');
        boolean needComma = false;
        while (next() != '}') {
            if (needComma) assertLast(',');
            final String key = readString();
            assertRead(':');

            final Object value = next() == '{' ? readMap() : readString();
            map.put(key, value);
            needComma = true;
        }

        return map;
    }
}
