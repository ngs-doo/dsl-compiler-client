package com.dslplatform.compiler.client.api.model.http;

import com.fasterxml.jackson.annotation.*;

public final class Header implements java.io.Serializable {
    public Header(
            final String key,
            final String value) {
        setKey(key);
        setValue(value);
    }

    public Header() {
        this.key = "";
        this.value = "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 51195639;
        result = prime * result + (this.key != null ? this.key.hashCode() : 0);
        result = prime * result
                + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof Header)) return false;
        final Header other = (Header) obj;

        if (!(this.key.equals(other.key))) return false;
        if (!(this.value.equals(other.value))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Header(" + key + ',' + value + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String key;

    @JsonProperty("key")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getKey() {
        return key;
    }

    public Header setKey(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"key\" cannot be null!");
        this.key = value;

        return this;
    }

    private String value;

    @JsonProperty("value")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getValue() {
        return value;
    }

    public Header setValue(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"value\" cannot be null!");
        this.value = value;

        return this;
    }
}
