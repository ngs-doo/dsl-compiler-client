package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RegisterTransportTest extends HttpTransportImplTest {

    @Test
    public void testRegisterUserRequestNotPermitted() throws IOException {
        final HttpRequest registerUserRequest; {
            final String email = "user@test.org";
            registerUserRequest = httpRequestBuilder.registerUser(email);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(registerUserRequest);
        assertEquals(201, parseResponse.code);
    }

    @Test
    public void testRegisterUserRequestEmailMissing() throws IOException {
        final HttpRequest registerUserRequest; {
            final String email = "";
            registerUserRequest = httpRequestBuilder.registerUser(email);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(registerUserRequest);
        assertEquals(201, parseResponse.code);
    }
}
