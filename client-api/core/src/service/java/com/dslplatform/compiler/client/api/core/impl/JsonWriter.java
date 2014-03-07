package com.dslplatform.compiler.client.api.core.impl;

import java.util.Map;

public class JsonWriter {
    private final StringBuilder sb;

    public JsonWriter() {
        sb = new StringBuilder();
    }

    public void write(final char value) {
        sb.append(value);
    }

    public void write(final String value) {
        sb.append('"');
        int last = 0;

        loop: for (int index = 0; index < value.length(); index++) {
            final char c = value.charAt(index);
            final char special;

            switch (c) {
                case 0x08: special = 'b'; break;
                case 0x09: special = 't'; break;
                case 0x0a: special = 'n'; break;
                case 0x0c: special = 'f'; break;
                case 0x0d: special = 'r'; break;

                case 0x22:
                case 0x2f:
                case 0x5c: special = c; break;

                default:
                    if (c <= 0x1f || c >= 0x7f && c <= 0x9f || c == 0xad ||
                            c >= 0x0600 && c <= 0x0604 ||
                            c == 0x070f || c == 0x17b4 || c == 0x17b5 ||
                            c >= 0x200c && c <= 0x200f ||
                            c >= 0x2028 && c <= 0x202f ||
                            c >= 0x2060 && c <= 0x206f ||
                            c == 0xfeff || c >= 0xfff0) {
                        sb.append(value.subSequence(last, index))
                          .append(String.format("\\u%04x", c));
                        last = index + 1;
                    }
                    continue loop;
            }
            sb.append(value.substring(last, index))
              .append('\\')
              .append(special);
            last = index + 1;
        }

        sb.append(value.substring(last)).append('"');
    }

    public void write(final Map<String, String> values) {
        write('{');
        boolean needComma = false;
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            if (needComma) write(',');
            write(entry.getKey());
            write(':');
            write(entry.getValue());
            needComma = true;
        }
        write('}');
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
