package com.dslplatform.compiler.client.api.core;

import com.dslplatform.client.*;
import com.dslplatform.compiler.client.api.model.Client.*;
import com.dslplatform.patterns.ServiceLocator;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.*;

public class ActionImpl implements Actions {

    private Logger logger;

    final String username;
    final String password;
    final String projectIni;
    private final ByteArrayInputStream bais;;
    public final ServiceLocator locator;

    public ActionImpl(final String username, final String password)  {

        this.username = username;
        this.password = password;

        try {
            projectIni = "api-url=" + baseurl + "/\n" + "package-name=com.dslplatform.compiler.client.api.model";
            bais = new ByteArrayInputStream(projectIni.getBytes("UTF8"));

            locator = Bootstrap.init(bais);
            logger = locator.resolve(Logger.class);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private void addAuth(HttpRequestBase hr) {
        hr.setHeader("Authorization", "Token " + makeToken());
    }

    private void addAuth(HttpRequestBase hr, final String projectID) {
        hr.setHeader("Authorization", "Token " + makeToken(projectID));
    }

    private void addAuth(HttpRequestBase hr, final byte[] token) throws UnsupportedEncodingException {
        hr.setHeader("Authorization", "Token " + new String(token, "UTF8"));
    }

    private void addHeader(HttpRequestBase hr, final String key, final Set<String> value) {
        if (value != null) for (final String header : value) hr.addHeader(key, header);
    }

    private String baseurl = "http://10.5.6.100/platform";
    private HttpClient http = HttpClients.createDefault();

    @Override
    public void parseDsl(final byte[] token, final String dsl) {
        String url = baseurl + "/parse";

        HttpPut req = new HttpPut(url);
        try {
            addAuth(req);
            for (final Header h : req.getAllHeaders()) {
                logger.trace("header:" + h.getName() + h.getValue());
            }
            req.setEntity(new StringEntity(dsl));

            logger.trace("{" + EntityUtils.toString(req.getEntity())+"}");
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void registerUser(final String email) {
        Register re = new Register().setEmail(email);
        try {
            re.submit(locator);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void createTestProject(final byte[] token, final String projectNick) {
        String url = baseurl + "/create/" + projectNick;

        HttpPost req = new HttpPost(url);
        try {
            addAuth(req);
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void createExternalProject(final byte[] token, final String projectName, final String serverName, final String applicationName, final DatabaseConnection databaseConnection) {
        eventPost(token, new CreateExternalProject(projectName, serverName, applicationName, databaseConnection));
    }

    @Override
    public void createUnmanagedProject(final byte[] token, final String serverName, final String applicationName, final DatabaseConnection databaseConnection) {

    }

    @Override
    public void downloadBinaries(final byte[] token, final UUID projectID) {

    }

    @Override
    public void downloadGeneratedModel(final byte[] token, final UUID projectID) {
        String url = baseurl + "/generated-model/" + projectID.toString();
        HttpGet req = new HttpGet(url);
        try {
            addAuth(req);
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void inspectUnmanagedProjectChanges(final byte[] token, final DatabaseConnection databaseConnection, final String newdsl, final String version) {

    }

    @Override
    public void inspectProjectChanges(final byte[] token, final UUID projectID, final String newdsl) {
        String url = baseurl + "/changes/" + projectID;

        HttpPut req = new HttpPut(url);
        try {
            addAuth(req);
            req.setEntity(new StringEntity(newdsl));
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void getLastManagedDSL(final byte[] token, final UUID projectID) {
        String url = baseurl + "/dsl/" + projectID.toString();

        HttpGet req = new HttpGet(url);
        try {
            addAuth(req);
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void getLastUnManagedDSL(final byte[] token, final DatabaseConnection databaseConnection) {

    }

    @Override
    public void getConfig(final byte[] token, final UUID projectID, final Set<String> targets, final String ns, final Set<String> options) {
        String url = baseurl + "/config/" + projectID.toString();

        HttpGet req = new HttpGet(url);
        try {
            addAuth(req);
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void diffWithLastDslUnmanaged(final byte[] token, final DatabaseConnection databaseConnection) {

    }

    @Override
    public void diffWithLastDslManaged(final byte[] token, final UUID projectID) {

    }

    @Override
    public void updateManagedProject(final byte[] token, final UUID projectID, final String namespace, final String migration, final String dsl, final Set<String> targets, final Set<String> options) {

    }

    @Override
    public void updateUnManagedProject(final byte[] token, final DatabaseConnection databaseConnection, final String namespace, final String dsl, final Set<String> targets, final Set<String> options) {

    }

    @Override
    public void generateMigrationSQL(final byte[] token, final String oldDsl, final String newDsl, final String version) {
        final String url = baseurl + "/unmanaged/postgres-migration";

        HttpPut req = new HttpPut(url);
        logger.trace("URL:" + req.getURI().toString());
        final String change = "{\"Old\":" + oldDsl + ",\"New\":" + newDsl + "\"}";
        try {
            addAuth(req);
            req.addHeader("version", version);
            for (final Header h : req.getAllHeaders()) {
                logger.trace("header:" + h.getName() + h.getValue());
            }

            req.setEntity(new StringEntity(change));

            logger.trace("payload: {}", EntityUtils.toString(req.getEntity()));
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void generateSources(final byte[] token, final UUID projectID, final String namespace, final Set<String> targets, final Set<String> options) {
        final String url = baseurl + "/source/" + projectID.toString();

        HttpGet req = new HttpGet(url);
        try {
            addAuth(req);
            addHeader(req, "targets", targets);
            addHeader(req, "options", options);
            req.setHeader("namespace", namespace);
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void generateUnmanagedSources(
            final byte[] token,
            final String namespace,
            final Set<String> targets,
            final Set<String> options,
            final String dsl) {
        final String url = baseurl + "/unmanaged/source" ;

        HttpPut req = new HttpPut(url);
        try {
            addAuth(req);
            addHeader(req, "targets", targets);
            addHeader(req, "options", options);
            req.setHeader("namespace", namespace);
            req.setEntity(new StringEntity(dsl));
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void getProjectByName(final byte[] token, final String projectName) {
        final String url = baseurl + "/Domain.svc/search/Client.Project";
        HttpPut req = new HttpPut(url);
        try {
            final String payload = JsonSerialization.serialize(new Project.FindByUserAndName(username, projectName));
            addAuth(req, token);
            req.addHeader("specification", "FindByUserAndName");
            req.setEntity(new StringEntity(payload));
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Override
    public void getAllProjects(final byte[] token) {
        final String url = baseurl + "/Domain.svc/search/Client.Project";
        HttpGet req = new HttpGet(url);
        try {
            logger.trace("URL: {}", req.getURI());
            addAuth(req, token);
            for (final Header h : req.getAllHeaders()) {
                logger.trace("header: [{}] [{}]", h.getName(), h.getValue());
            }
            final HttpResponse response = http.execute(req);
            final String responseStr = EntityUtils.toString(response.getEntity());
            logger.trace(responseStr);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Override
    public void renameProject(final byte[] token, final String oldName, final String newName) {
        eventPost(token, new RenameProject(oldName, newName));
    }

    @Override
    public void cleanProject(final byte[] token) {
        eventPost(token, new WipeCleanProject());
    }

    @Override
    public void templateGet(final byte[] token, final String templateName) {

    }

    @Override
    public void templateCreate(final byte[] token, final String templateName) {

    }

    @Override
    public void templateListAll(final byte[] token, final String templateName) {

    }

    @Override
    public void templateDelete(final byte[] token, final String templateName) {

    }

    public byte[] makeToken() {
        return makeToken(null);
    }

    public byte[] makeToken(final String uuid)  {
        final Charset charset = Charset.forName("UTF-8");
        final String algo = "RSA";

        final String noproject = username + ":" + password + ":" + System.currentTimeMillis() / 1000;
        final String toToken = (uuid != null) ? noproject + ":" + uuid : noproject;
        final byte[] message = toToken.getBytes(charset);

        final InputStream keyStream = getClass().getResourceAsStream("/ngs-rsa.crt.der");

        final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X509");

            final Certificate cert = cf.generateCertificate(keyStream);
            final PublicKey pkey = cert.getPublicKey();

            final Cipher cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE, pkey);
            final byte[] ct64 = Base64.encodeBase64(cipher.doFinal(message));
            logger.trace("Made token form {}:", toToken, new String(ct64, charset));
            return ct64;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private <T> String eventPost(final byte[] token, final T event) {
        final String url = baseurl + "/Domain.svc/submit/Client." + event.getClass().getSimpleName();
        final HttpPost req = new HttpPost(url);
        try {
            req.setEntity(new StringEntity(JsonSerialization.serialize(event)));
            addAuth(req, token);
            final HttpResponse response = http.execute(req);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}

