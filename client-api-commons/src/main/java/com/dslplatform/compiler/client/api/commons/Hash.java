package com.dslplatform.compiler.client.api.commons;

import java.io.Serializable;
import java.util.Arrays;

import com.dslplatform.compiler.client.api.commons.codec.digest.DigestUtils;

public class Hash implements Serializable {
    public final byte[] hash;

// ----------------------------------------------------------------------------

    public final int hashCode;

// ----------------------------------------------------------------------------

    public Hash(final byte[] body) {
        this.hash = DigestUtils.sha1(body);
        this.hashCode = HashUtil.hashCode(hash);
    }

    private Hash(final byte[] hash, final int hashCode) {
        this.hash = hash;
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Hash))
            return false;

        final Hash h = (Hash) o;
        return (hashCode == h.hashCode) && Arrays.equals(hash, h.hash);
    }

// ----------------------------------------------------------------------------

    public Hash toHash() {
        return new Hash(hash, hashCode);
    }

    public static class Body extends Hash {
        public final byte[] body;

        public Body(final byte[] body) {
            super(body);
            this.body = body;
        }

        private static final long serialVersionUID = 0x0L;
    }

// ----------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("%08X", hashCode);
    }

    private static final long serialVersionUID = 0x0L;
}
