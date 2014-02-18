package com.dslplatform.compiler.client.api.model.http;

import com.fasterxml.jackson.annotation.*;

public final class Request implements java.io.Serializable {
    public Request(
            final java.net.URI url,
            final com.dslplatform.compiler.client.api.model.http.Method method,
            final java.util.List<com.dslplatform.compiler.client.api.model.http.Header> headers,
            final byte[] body) {
        setUrl(url);
        setMethod(method);
        setHeaders(headers);
        setBody(body);
    }

    public Request() {
        this.method = com.dslplatform.compiler.client.api.model.http.Method.GET;
        this.headers = new java.util.ArrayList<com.dslplatform.compiler.client.api.model.http.Header>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 1534390763;
        result = prime * result + (this.url != null ? this.url.hashCode() : 0);
        result = prime * result + (this.method.hashCode());
        result = prime * result + (java.util.Arrays.hashCode(this.body));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof Request)) return false;
        final Request other = (Request) obj;

        if (!(this.url.equals(other.url))) return false;
        if (!(this.method.equals(other.method))) return false;
        if (!(this.headers.equals(other.headers))) return false;
        if (!(java.util.Arrays.equals(this.body, other.body))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Request(" + url + ',' + method + ',' + headers + ',' + body
                + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private java.net.URI url;

    @JsonProperty("url")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public java.net.URI getUrl() {
        return url;
    }

    public Request setUrl(final java.net.URI value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"url\" cannot be null!");
        this.url = value;

        return this;
    }

    private com.dslplatform.compiler.client.api.model.http.Method method;

    @JsonProperty("method")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public com.dslplatform.compiler.client.api.model.http.Method getMethod() {
        return method;
    }

    public Request setMethod(
            final com.dslplatform.compiler.client.api.model.http.Method value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"method\" cannot be null!");
        this.method = value;

        return this;
    }

    private java.util.List<com.dslplatform.compiler.client.api.model.http.Header> headers;

    @JsonProperty("headers")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public java.util.List<com.dslplatform.compiler.client.api.model.http.Header> getHeaders() {
        return headers;
    }

    public Request setHeaders(
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
    public byte[] getBody() {
        return body;
    }

    public Request setBody(final byte[] value) {
        this.body = value;

        return this;
    }
}
