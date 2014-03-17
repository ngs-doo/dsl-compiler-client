package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class UpdateManagedProjectTransportTest extends HttpTransportImplTest {

    @Test
    public void testUpdateManagedProjectRequest() throws IOException {
        final HttpRequest updateManagedProjectRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            final String migration = "";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};
            updateManagedProjectRequest = httpRequestBuilder.updateManagedProject(token, UUID.fromString(validID), targets, migration, packageName, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(updateManagedProjectRequest);
        assertEquals(201, response.code);
    }
}
