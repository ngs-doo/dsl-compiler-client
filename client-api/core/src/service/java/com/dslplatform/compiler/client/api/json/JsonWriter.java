package com.dslplatform.compiler.client.api.json;

import javax.xml.bind.DatatypeConverter;
import java.util.Map;

public class JsonWriter {
    private final StringBuilder sb;

    public JsonWriter() {
        sb = new StringBuilder();
    }

    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final char[] ASCII = new char[0x100 << 3];

    static {
        for (char c = 0; c < 0x100; c++) {
            final int c8 = c << 3;
            final char special;

            switch (c) {
                case 0x08: special = 'b'; break;
                case 0x09: special = 't'; break;
                case 0x0a: special = 'n'; break;
                case 0x0c: special = 'f'; break;
                case 0x0d: special = 'r'; break;
                case 0x22: special = '"'; break;
                case 0x2f: special = '/'; break;
                case 0x5c: special = '\\'; break;

                default:
                    if (c <= 0x1f || c >= 0x7f && c <= 0x9f || c == 0xad) {
                        ASCII[c8] = 6;
                        ASCII[c8 + 1] = '\\';
                        ASCII[c8 + 2] = 'u';
                        ASCII[c8 + 3] = '0';
                        ASCII[c8 + 4] = '0';
                        ASCII[c8 + 5] = HEX[(c >>> 4) & 0xf];
                        ASCII[c8 + 6] = HEX[c & 0xf];
                    }
//                    else {
//                        ASCII[c8] = 1;
//                        ASCII[c8 + 1] = c;
//                    }
                    continue;
            }

            ASCII[c8] = 2;
            ASCII[c8 + 1] = '\\';
            ASCII[c8 + 2] = special;
        }
    }

    public JsonWriter write(final String value) {
        final int length = value.length();
        if (length == 0) {
            sb.append("\"\"");
            return this;
        }

        final char[] values = value.toCharArray();
        sb.append('"');
        int last = 0;
        for (int index = 0; index < length; index++) {
            final char c = values[index];
            if (c < 0x100) {
                final int c8 = c << 3;
                final int cLen = ASCII[c8];
                if (cLen > 0) {
                    sb.append(values, last, index - last);
                    sb.append(ASCII, c8 + 1, cLen);
                    last = index + 1;
                }
            } else if (c >= 0x0600 && c <= 0x0604 ||
                    c == 0x070f || c == 0x17b4 || c == 0x17b5 ||
                    c >= 0x200c && c <= 0x200f ||
                    c >= 0x2028 && c <= 0x202f ||
                    c >= 0x2060 && c <= 0x206f ||
                    c == 0xfeff || c >= 0xfff0) {
                sb.append(values, last, index - last);
                sb.append(new char[] {
                    '\\',
                    'u',
                    HEX[(c >>> 12) & 0xf],
                    HEX[(c >>>  8) & 0xf],
                    HEX[(c >>>  4) & 0xf],
                    HEX[c & 0xf]});
                last = index + 1;
            }
        }

        sb.append(values, last, length - last);
        sb.append('"');
        return this;
    }

    @SuppressWarnings("unchecked")
    public JsonWriter write(final Map<String, Object> values) {
        sb.append('{');
        boolean needComma = false;
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            if (needComma) sb.append(',');
            write(entry.getKey());
            sb.append(':');

            final Object value = entry.getValue();
            if (value instanceof Map) {
                write((Map<String, Object>) value);
            }
            else if (value instanceof byte[]) {
                write((byte[])value);
            } else if (value instanceof Integer) {
                sb.append((int)value);
            } else if (value instanceof String) {
                write(value.toString());
            } else {
                throw new RuntimeException("Serialization not implemented!");
            }
            needComma = true;
        }
        sb.append('}');
        return this;
    }

    public JsonWriter write(final String[] values) {
        sb.append('[');
        for (int i = 0; i < values.length; i ++) {
            if (i > 0) sb.append(',');
            write(values[i]);
        }
        sb.append(']');
        return this;
    }

    public JsonWriter write(final byte[] value) {
        sb.append('"');
        sb.append(DatatypeConverter.printBase64Binary(value));
        sb.append('"');
        return this;
    }

    public static String escape(final String value) {
        return new JsonWriter().write(value).toString();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
