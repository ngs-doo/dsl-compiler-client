package com.dslplatform.compiler.client.cmdline.parser;
import java.util.ArrayDeque;
import java.util.Queue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.io.PathExpander;

public class SanityTests {

    final static Logger logger = LoggerFactory.getLogger(SanityTests.class);

    @Test
    public void shouldNotThrowTest() throws Exception {

        final StringBuilder sb = new StringBuilder();

        final Queue<String> q = new ArrayDeque<String>();

        q.add("-i");
        q.add("kekec");
        q.add("-ibekec");
        q.add("tukac");
        q.add("-ipero");
        q.add("--project-id=pero");
        q.add("pero2");
        q.add("--project-id=x");
        q.add("y");
        q.add("-iqwe");
        q.add("--project-id=1-2-3-4-5");
        q.add("-uu");
        q.add("--with-active-record");
        q.add("--with-active-record=yes");
        q.add("-f~/code/ngs/dsl-compiler-client/client-api/cmdlineparser/src/test/resources/example.props");
        q.add("--cache-path=~/code/ngs/dsl-compiler-client/client-api/cmdlineparser/src/test/resources/cache");
        q.add("-tjava client,java,scala-server");
        q.add("--project-name");
        q.add("KiklopZdenski");
        q.add("--package-name=com.`dsl platform`.test");
        q.add("--target=!java,c# server");
        q.add("update");
        q.add("config");
        q.add("parse");

        final PropertyLoader propertyLoader = new PropertyLoader(logger,
                new StreamLoader(logger, new PathExpander(logger)));

        final Arguments a = new CachingArgumentsProxy(
                logger,
                new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(q)));


        System.out.println(a.getActions().getActionSet().toString());
        System.out.println(a.getCachePath());
        System.out.println(a.getDSLPath());
        System.out.println(a.getLoggingLevel());
        System.out.println(a.getOutputPath());
        System.out.println(a.getPackageName());
        System.out.println(a.getPassword());
        System.out.println(a.getProjectID());
        System.out.println(a.getProjectName());
        System.out.println(a.getProjectPropertiesPath());
        System.out.println(a.getTargets());
        System.out.println(a.getUsername());
        System.out.println(a.getPassword());

    }
}
