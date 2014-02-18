package com.dslplatform.compiler.client.api.model.Client.repositories;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.dslplatform.client.DomainProxy;
import com.dslplatform.compiler.client.api.model.Client.DeleteTemplate;
import com.dslplatform.compiler.client.api.model.Client.UploadTemplate;

public class TemplateEvents {
    private final Logger logger;
    private final DomainProxy domainProxy;

    public TemplateEvents(final Logger logger, final DomainProxy domainProxy) {
        this.logger = logger;
        this.domainProxy = domainProxy;
    }

    public void upload(final UUID projectID, final String filename, final byte[] content) throws IOException {
        try {
            domainProxy.submit(
                    new UploadTemplate().setProject(projectID.toString()).setName(filename).setContent(content)).get();

            logger.debug("Successfully uploaded template of {} bytes: {}", content.length, filename);
        } catch (final InterruptedException e) {
            throw new IOException(e);
        } catch (final ExecutionException e) {
            throw new IOException(e);
        }
    }

    public void delete(final UUID projectID, final String filename) throws IOException {
        try {
            domainProxy.submit(new DeleteTemplate().setProject(projectID.toString()).setName(filename)).get();

            logger.debug("Successfully deleted template: {}", filename);
        } catch (final InterruptedException e) {
            throw new IOException(e);
        } catch (final ExecutionException e) {
            throw new IOException(e);
        }
    }
}
