package com.dslplatform.compiler.client.cmdline.parser;
import java.util.ArrayDeque;
import java.util.Queue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.io.PathExpander;
import com.dslplatform.compiler.client.params.Action;

public class ActionsTest {

    final static Logger logger = LoggerFactory.getLogger(ActionsTest.class);

    @Test
    public void shouldNotThrowTest() throws Exception {

        final StringBuilder sb = new StringBuilder();

        final Queue<String> q = new ArrayDeque<String>();

        q.add(Action.UPDATE.toString());

        q.add(Action.CONFIG.toString());

        q.add(Action.PARSE.toString());

        q.add(Action.GET_CHANGES.toString());

        q.add(Action.GENERATE_SOURCES.toString());

        q.add(Action.DOWNLOAD_GENERATED_MODEL.toString());

        q.add(Action.UNMANAGED_SQL_MIGRATION.toString());


        final PropertyLoader propertyLoader = new PropertyLoader(logger,
                new StreamLoader(logger, new PathExpander(logger)));

        final Arguments a = new CachingArgumentsProxy(
                logger,
                new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(q)));


        System.out.println(a.getActions().getActionSet().toString());
    }
}
