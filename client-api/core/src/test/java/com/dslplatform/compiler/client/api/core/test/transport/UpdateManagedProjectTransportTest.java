package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import org.apache.commons.codec.Charsets;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class UpdateManagedProjectTransportTest extends HttpTransportImplTest {

    @Test
    public void testUpdateManagedProjectRequest() throws IOException {
        final HttpRequest updateManagedProjectRequest; {
            final String token = userToken(validUser, validPassword);
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            final String migration = "safe";
            final Map<String, String> dsl = MockData.managed_dsl_AB;

            updateManagedProjectRequest = httpRequestBuilder.updateManagedProject(token, UUID.fromString(validId), targets, packageName, migration, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(updateManagedProjectRequest);
        logger.info(new String(response.body, Charsets.UTF_8));
        assertEquals(201, response.code);
    }
}
