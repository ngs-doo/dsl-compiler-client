package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.RegisterUserResponse;

import org.apache.commons.codec.Charsets;

public class RegisterUserProcessor {
    public RegisterUserResponse process(final HttpResponse httpResponse) {
        final boolean success = httpResponse.code == 200;
        final String authErrorMsg = success ? null : new String (httpResponse.body, Charsets.UTF_8);
        return new RegisterUserResponse(success, authErrorMsg);
    }
}
