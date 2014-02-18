import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.client.Bootstrap;
import com.dslplatform.client.CredentialsAuthorization;
import com.dslplatform.client.DomainProxy;
import com.dslplatform.client.HttpAuthorization;
import com.dslplatform.client.JsonSerialization;
import com.dslplatform.compiler.client.api.config.ClientConfiguration;
import com.dslplatform.compiler.client.api.config.ClientConfigurationFactory;
import com.dslplatform.compiler.client.api.config.PropertyClientConfigurationFactory;
import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.api.io.HttpRequest;
import com.dslplatform.compiler.client.api.model.Client.ProjectDetails;
import com.dslplatform.compiler.client.api.model.Client.repositories.ProjectRepository;
import com.dslplatform.compiler.client.api.model.Client.repositories.TemplateEvents;
import com.dslplatform.compiler.client.api.model.http.Method;
import com.dslplatform.compiler.client.api.model.http.Request;
import com.dslplatform.compiler.client.api.model.http.Response;
import com.dslplatform.compiler.client.util.PathExpander;
import com.dslplatform.patterns.ServiceLocator;

public class Main {
    public static void main(final String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

        final Logger logger = LoggerFactory.getLogger(Main.class);
        final PathExpander pathExpander = new PathExpander(logger);
        final StreamLoader streamLoader = new StreamLoader(logger, pathExpander);
        final PropertyLoader propertyLoader = new PropertyLoader(logger, streamLoader);
        final String configurationPath = "/api.properties";
        final ClientConfigurationFactory configurationFactory =
                new PropertyClientConfigurationFactory(logger, propertyLoader, configurationPath);

        final ClientConfiguration clientConfiguration = configurationFactory.getClientConfiguration();

        final String username = "marko@element.hr";
        final char[] password = "password".toCharArray();
        final String projectIni =
                "api-url=" + clientConfiguration.getCompilerUri() + "\n"
                        + "package-name=com.dslplatform.compiler.client.api.model";
        final ByteArrayInputStream bais = new ByteArrayInputStream(projectIni.getBytes("ISO-8859-1"));

        final HttpAuthorization httpAuthorization = new CredentialsAuthorization(username, new String(password));
        final HttpRequest httpRequest = new HttpRequest(logger, clientConfiguration, streamLoader, httpAuthorization);

        final Map<Class<?>, Object> locatorComponents = new HashMap<Class<?>, Object>();
        locatorComponents.put(Logger.class, logger);
        locatorComponents.put(HttpAuthorization.class, httpAuthorization);

        final ServiceLocator locator = Bootstrap.init(bais, locatorComponents);
        final JsonSerialization jsonDeserialization = locator.resolve(JsonSerialization.class);

        {
            final Request request =
                    httpRequest.builder(Method.PUT, "parse").setBody("{\"a.dsl\":\"module a; xXx\"}".getBytes("UTF-8"));
            httpRequest.sendRequest(request);
        }

        final DomainProxy domainProxy = locator.resolve(DomainProxy.class);
        final ProjectRepository projectRepository = new ProjectRepository(logger, domainProxy, username);
        final TemplateEvents templateEvents = new TemplateEvents(logger, domainProxy);

        final ProjectDetails projectDetails = projectRepository.getProject("extest");

        {
            final Request request = httpRequest.builder(Method.GET, "dsl/" + projectDetails.getID());

            final Response response = httpRequest.sendRequest(request);
            jsonDeserialization.deserialize(JsonSerialization.buildGenericType(LinkedHashMap.class, String.class,
                    String.class), new String(response.getBody(), "UTF-8"));
        }

        {
            final Request request = httpRequest.builder(Method.GET, "generated-model/" + projectDetails.getID());
            httpRequest.sendRequest(request);
        }

        {
            final Request request = httpRequest.builder(Method.GET, "download/" + projectDetails.getID());
            httpRequest.sendRequest(request);
        }

        {
            templateEvents.upload(projectDetails.getID(), "keke.docx", "Fake DOCX!".getBytes("UTF-8"));
        }

        {
            final Request request = httpRequest.builder(Method.GET, "templates/" + projectDetails.getID());
            final Response response = httpRequest.sendRequest(request);

            System.out.println(new String(response.getBody()));
        }

        {
            final Request request =
                    httpRequest.builder(Method.GET, "template/" + projectDetails.getID() + "/keke.docx");
            final Response response = httpRequest.sendRequest(request);

            final String content =
                    new String(Base64.decodeBase64(((String) jsonDeserialization.deserialize(
                            JsonSerialization.buildType(String.class), new String(response.getBody(), "UTF-8")))
                            .getBytes("UTF-8")), "UTF-8");

            System.out.println(content);
        }

        {
            templateEvents.delete(projectDetails.getID(), "keke.docx");
        }

        {
            final Request request =
                    httpRequest
                            .builder(
                                    Method.POST,
                                    "update/"
                                            + projectDetails.getID()
                                            + "?targets=Java,ScalaClient&namespace=test.update&migration=unsafe&options=with-active-record")
                            .setBody("{\"a.dsl\":\"module Foo { root Bar { } }\"}".getBytes("UTF-8"));

            final Response response = httpRequest.sendRequest(request);

            jsonDeserialization.deserialize(JsonSerialization.buildGenericType(LinkedHashMap.class, String.class,
                    String.class), new String(response.getBody(), "UTF-8"));
        }

        {
            final Request request =
                    httpRequest.builder(Method.GET, "source/" + projectDetails.getID()
                            + "?targets=PHP&namespace=test.source&options=with-active-record");

            final Response response = httpRequest.sendRequest(request);
            jsonDeserialization.deserialize(JsonSerialization.buildGenericType(LinkedHashMap.class, String.class,
                    String.class), new String(response.getBody(), "UTF-8"));
        }

        {
            final Request request =
                    httpRequest.builder(Method.PUT, "changes/" + projectDetails.getID()).setBody(
                            "{\"a.dsl\":\"module Foo { root Xyz; }\"}".getBytes("UTF-8"));

            final Response response = httpRequest.sendRequest(request);
            System.out.println(new String(response.getBody()));
        }

        {
            final Request request =
                    httpRequest.builder(Method.GET, "config/" + projectDetails.getID()
                            + "?targets=PHP,Java,CSharp,Scala&namespace=test.config");

            final Response response = httpRequest.sendRequest(request);
            jsonDeserialization.deserialize(JsonSerialization.buildGenericType(LinkedHashMap.class, String.class,
                    String.class), new String(response.getBody(), "UTF-8"));
        }

//        CleanProject;
//        CloneProject;
//        CreateProject;
//        DeleteProject;
//        GenerateMigrationSQL;

        locator.resolve(ExecutorService.class).shutdown();
    }
}
