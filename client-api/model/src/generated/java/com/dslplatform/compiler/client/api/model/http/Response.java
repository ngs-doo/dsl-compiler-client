package com.dslplatform.compiler.client.api.model.http;

import com.fasterxml.jackson.annotation.*;

public final class Response implements java.io.Serializable {
    public Response(
            final int code,
            final String status,
            final java.util.List<com.dslplatform.compiler.client.api.model.http.Header> headers,
            final byte[] body) {
        setCode(code);
        setStatus(status);
        setHeaders(headers);
        setBody(body);
    }

    public Response() {
        this.code = 0;
        this.status = "";
        this.headers = new java.util.ArrayList<com.dslplatform.compiler.client.api.model.http.Header>();
        this.body = new byte[0];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 807705055;
        result = prime * result + (this.code);
        result = prime * result
                + (this.status != null ? this.status.hashCode() : 0);
        result = prime * result + (java.util.Arrays.hashCode(this.body));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof Response)) return false;
        final Response other = (Response) obj;

        if (!(this.code == other.code)) return false;
        if (!(this.status.equals(other.status))) return false;
        if (!(this.headers.equals(other.headers))) return false;
        if (!(java.util.Arrays.equals(this.body, other.body))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Response(" + code + ',' + status + ',' + headers + ',' + body
                + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private int code;

    @JsonProperty("code")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public int getCode() {
        return code;
    }

    public Response setCode(final int value) {
        this.code = value;

        return this;
    }

    private String status;

    @JsonProperty("status")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getStatus() {
        return status;
    }

    public Response setStatus(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"status\" cannot be null!");
        this.status = value;

        return this;
    }

    private java.util.List<com.dslplatform.compiler.client.api.model.http.Header> headers;

    @JsonProperty("headers")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public java.util.List<com.dslplatform.compiler.client.api.model.http.Header> getHeaders() {
        return headers;
    }

    public Response setHeaders(
            final java.util.List<com.dslplatform.compiler.client.api.model.http.Header> value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"headers\" cannot be null!");
        com.dslplatform.compiler.client.api.model.Guards.checkNulls(value);
        this.headers = value;

        return this;
    }

    private byte[] body;

    @JsonProperty("body")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public byte[] getBody() {
        return body;
    }

    public Response setBody(final byte[] value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"body\" cannot be null!");
        this.body = value;

        return this;
    }
}
