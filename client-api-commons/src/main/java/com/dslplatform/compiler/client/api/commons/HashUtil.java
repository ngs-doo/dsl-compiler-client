package com.dslplatform.compiler.client.api.commons;

import java.util.UUID;
import java.util.zip.CRC32;

public class HashUtil {
    private HashUtil() {}

    public static int hashCode(final byte[] body) {
        final CRC32 crc32 = new CRC32();
        crc32.update(body);
        return (int) crc32.getValue();
    }

    public static int hashCode(final UUID body) {
        final CRC32 crc32 = new CRC32();
        for (int i = 7; i >= 0; i--) {
            crc32.update((byte) (body.getLeastSignificantBits() >>> (i << 3)));
        }

        for (int i = 7; i >= 0; i--) {
            crc32.update((byte) (body.getMostSignificantBits() >>> (i << 3)));
        }
        return (int) crc32.getValue();
    }
}
