package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsReader;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsValidator;
import com.dslplatform.compiler.client.cmdline.parser.CachingArgumentsProxy;
import com.dslplatform.compiler.client.io.PathExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    public static void main(String... argv) throws IOException {
        final Logger logger = LoggerFactory.getLogger("dsl-clc");

        final PropertyLoader propertyLoader = new PropertyLoader(logger,
                new StreamLoader(logger, new PathExpander(logger)));

        final Arguments arguments = new CachingArgumentsProxy(
                logger,
                new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(argv)));
        final CLCAction clcAction = new ActionDefinition(logger, arguments);
        //System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", arguments.getLoggingLevel().level);
        process(clcAction);
    }

    public static void process(CLCAction clcAction) throws IOException {
        clcAction.process();
    }
}
