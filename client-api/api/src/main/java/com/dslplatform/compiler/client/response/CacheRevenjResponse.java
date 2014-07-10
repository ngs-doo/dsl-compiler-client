package com.dslplatform.compiler.client.response;

public class CacheRevenjResponse {
    public final boolean success;
    public final String message;

    public CacheRevenjResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
