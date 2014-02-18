package com.dslplatform.compiler.client.api.model.Client.repositories;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.dslplatform.client.DomainProxy;
import com.dslplatform.compiler.client.api.model.Client.Project;
import com.dslplatform.compiler.client.api.model.Client.ProjectDetails;
import com.dslplatform.patterns.Specification;

public class ProjectRepository {
    private final Logger logger;
    private final DomainProxy domainProxy;
    private final String username;

    public ProjectRepository(final Logger logger, final DomainProxy domainProxy, final String username) {
        this.logger = logger;
        this.domainProxy = domainProxy;
        this.username = username;
        projectSearch = new Project.FindByUser(username);
    }

    private static ProjectDetails toProjectDetails(final Project project) {
        return new ProjectDetails().setID(project.getID()).setProjectName(project.getNick())
                .setCreatedAt(project.getCreatedAt());
    }

    public ProjectDetails getProject(final String projectName) throws IOException {
        final Specification<Project> oneProjectSearch = new Project.FindByUserAndName(username, projectName);
        try {
            final List<Project> project = domainProxy.search(oneProjectSearch, 1, 0).get();
            if (project.isEmpty()) {
                logger.warn("Could not find project with name: {}", projectName);
                return null;
            }

            logger.debug("Found project name: {}", projectName);
            return toProjectDetails(project.get(0));
        } catch (final ExecutionException e) {
            throw new IOException(e);
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
    }

    private final Specification<Project> projectSearch;

    private static final Iterable<Map.Entry<String, Boolean>> projectOrdering = Arrays
            .asList((Map.Entry<String, Boolean>) new AbstractMap.SimpleEntry<String, Boolean>("CreatedAt", false));

    public List<ProjectDetails> getProjects() throws IOException {
        try {
            final List<Project> projects = domainProxy.search(projectSearch, null, null, projectOrdering).get();

            final List<ProjectDetails> result = new ArrayList<ProjectDetails>(projects.size());
            for (final Project project : projects) {
                result.add(toProjectDetails(project));
            }

            logger.debug("Found {} projects", projects.size());
            return result;
        } catch (final ExecutionException e) {
            throw new IOException(e);
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
    }
}
