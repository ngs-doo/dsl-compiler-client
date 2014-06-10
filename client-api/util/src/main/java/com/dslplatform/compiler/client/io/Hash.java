package com.dslplatform.compiler.client.io;

import com.dslplatform.compiler.client.io.codec.digest.DigestUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.CRC32;

public class Hash implements Serializable {
    public final byte[] hash;

// ----------------------------------------------------------------------------

    public final int hashCode;

// ----------------------------------------------------------------------------

    public Hash(
            final byte[] body) {
        hash = DigestUtils.sha1(body);
        hashCode = hashCode(hash);
    }

    private Hash(
            final byte[] hash,
            final int hashCode) {
        this.hash = hash;
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Hash)) {
            return false;
        }

        final Hash h = (Hash) o;
        return hashCode == h.hashCode && Arrays.equals(hash, h.hash);
    }

// ----------------------------------------------------------------------------

    public static class Body extends Hash {
        public final byte[] body;

        public Body(
                final byte[] body) {
            super(body);
            this.body = body;
        }

        private static final long serialVersionUID = 0x0L;
    }

// ----------------------------------------------------------------------------

    private static final long serialVersionUID = 0x0L;

    @Override
    public String toString() {
        return String.format("%08X", hashCode);
    }

// ----------------------------------------------------------------------------

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
