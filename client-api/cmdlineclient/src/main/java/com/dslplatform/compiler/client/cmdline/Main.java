package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsReader;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsValidator;
import com.dslplatform.compiler.client.cmdline.parser.CachingArgumentsProxy;
import com.dslplatform.compiler.client.io.PathExpander;
import com.dslplatform.compiler.client.params.Action;
import com.dslplatform.compiler.client.params.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    public static void main(String ... argv) throws IOException {
        final Logger logger = LoggerFactory.getLogger("dsl-clc");

        final PropertyLoader propertyLoader = new PropertyLoader(logger,
                new StreamLoader(logger, new PathExpander(logger)));

        final Arguments arguments = new CachingArgumentsProxy(
                logger,
                new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(argv)));
        Actions actions = arguments.getActions();

        //System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", arguments.getLoggingLevel().level);
        for (Action action : actions.getActionSet()) {
            logger.trace("Performing action " + action.actionKey);
            CLCAction clcAction = new ActionDefinition(logger, arguments);
            performAction(clcAction, action);
        }
    }

    public static void performAction(CLCAction clcAction, Action action) throws IOException {
        switch (action) {
            case UPDATE:
                /* todo - managed action */
                break;
            case GET_CHANGES:
                clcAction.getChanges();
                break;
            case LAST_DSL:
                /* todo - missing use case */
                break;
            case CONFIG:
                /* todo - managed action */
                break;
            case PARSE:
                clcAction.parseDSL();
                break;
            case GENERATE_SOURCES:
                /* todo - managed action */
                break;
            case UNMANAGED_CS_SERVER:
                clcAction.deployUnmanagedServer();
                break;
            case UNMANAGED_SOURCE:
                clcAction.unmanagedSource();
                break;
            case UPGRADE_UNMANAGED_DATABASE:
                clcAction.upgradeUnmanagedDatabase();
                break;
            case UNMANAGED_SQL_MIGRATION:
                clcAction.upgradeUnmanagedDatabase();
                break;
            case DEPLOY_UNMANAGED_SERVER:
                clcAction.deployUnmanagedServer();
                break;
        }
    }
}
