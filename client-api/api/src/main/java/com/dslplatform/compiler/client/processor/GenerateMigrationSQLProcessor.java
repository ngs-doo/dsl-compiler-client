package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.GenerateMigrationSQLResponse;

import java.nio.charset.Charset;

public class GenerateMigrationSQLProcessor {
    public GenerateMigrationSQLResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;

        final boolean migrationRequestSuccessful = httpResponse.code == 200;
        final String migration = migrationRequestSuccessful ? httpResponseString : null;

        return new GenerateMigrationSQLResponse(authSuccess, authResponseMessage, migrationRequestSuccessful, migration);
    }
}
