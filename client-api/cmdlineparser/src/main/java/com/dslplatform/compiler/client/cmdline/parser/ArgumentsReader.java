package com.dslplatform.compiler.client.cmdline.parser;

import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.ALLOW_UNSAFE_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.CACHE_PATH_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.END_OF_PARAMS;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.LOGGING_LEVEL_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.OUTPUT_PATH_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.PACKAGE_NAME_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.PROJECT_NAME_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.PROJECT_PROPERTIES_PATH_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.SKIP_DIFF_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.TARGET_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.USERNAME_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.WITH_ACTIVE_RECORD_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.WITH_HELPER_METHODS_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.WITH_JACKSON_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.WITH_JAVA_BEANS_SWITCHES;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.slf4j.Logger;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.SwitchArgument;

public class ArgumentsReader {
    private final Logger logger;
    private final PropertyLoader propertyLoader;

    public ArgumentsReader(final Logger logger, final PropertyLoader propertyLoader) {
        this.logger = logger;
        this.propertyLoader = propertyLoader;
    }

    public Properties readArguments(final Queue<String> args) throws IOException {
        final Properties props = new Properties();

        while (true) {
            final String arg = args.peek();
            if (arg == null) {
                logger.trace("Stopping argument reader (end of argument list)");
                break;
            }

            if (END_OF_PARAMS.is(arg)) {
                logger.debug("Stopping argument reader (encountered end-of-params marker)");
                break;
            }

            args.poll();
            logger.trace("Read argument: {}", arg);

            final boolean isLast = args.isEmpty();
            if (isLast) logger.trace("This is the last argument ...");

            /* Regular one param switches */
            if (parseSingleParamArgumentOwerwriteOldArgs(USERNAME_SWITCHES, arg, args, props, isLast)) continue;
            if (parseSingleParamArgumentOwerwriteOldArgs(OUTPUT_PATH_SWITCHES, arg, args, props, isLast)) continue;
            if (parseSingleParamArgumentOwerwriteOldArgs(CACHE_PATH_SWITCHES, arg, args, props, isLast)) continue;
            if (parseSingleParamArgumentOwerwriteOldArgs(LOGGING_LEVEL_SWITCHES, arg, args, props, isLast)) continue;
            if (parseSingleParamArgumentOwerwriteOldArgs(PROJECT_NAME_SWITCHES, arg, args, props, isLast)) continue;
            if (parseSingleParamArgumentOwerwriteOldArgs(PACKAGE_NAME_SWITCHES, arg, args, props, isLast)) continue;

            if (parseSingleParamArgumentJoinOldArgs(TARGET_SWITCHES, arg, args, props, isLast)) continue;

            /* Boolean flag switches: */
            if (doBooleanFlagArg(WITH_ACTIVE_RECORD_SWITCHES, arg, args, props)) continue;
            if (doBooleanFlagArg(WITH_JAVA_BEANS_SWITCHES, arg, args, props)) continue;
            if (doBooleanFlagArg(WITH_JACKSON_SWITCHES, arg, args, props)) continue;
            if (doBooleanFlagArg(WITH_HELPER_METHODS_SWITCHES, arg, args, props)) continue;
            if (doBooleanFlagArg(SKIP_DIFF_SWITCHES, arg, args, props)) continue;
            if (doBooleanFlagArg(ALLOW_UNSAFE_SWITCHES, arg, args, props)) continue;

            /* Other switches */

            {
                final SwitchArgument sa = PROJECT_PROPERTIES_PATH_SWITCHES.examine(arg);
                if (sa.isSwitch) {
                    // parse ProjectPropertiesPath, new arguments overwrite old ones
                    logger.trace("Encountered switch [{}]", sa.getSwitch());
                    String projectPropertiesPath;

                    if (sa.hasBody()) {
                        projectPropertiesPath = sa.getArgument();
                        logger.trace("Parsed ProjectPropertiesPath argument [{}]", projectPropertiesPath);
                    } else {
                        if (isLast) { throw new IllegalArgumentException("ProjectPropertiesPath cannot be empty!"); }

                        projectPropertiesPath = args.poll();
                        logger.trace("ProjectPropertiesPath argument was empty, read next argument [{}]",
                                projectPropertiesPath);
                    }

                    logger.debug("Read ProjectPropertiesPath argument [{}]", projectPropertiesPath);
                    final Properties projectProperties = propertyLoader.read(projectPropertiesPath);

                    for (final Map.Entry<Object, Object> entry : projectProperties.entrySet()) {
                        final String key = String.valueOf(entry.getKey());
                        final String value = String.valueOf(entry.getValue());
                        final Object old = props.setProperty(key, value);

                        if (old == null) {
                            logger.trace("Importing properties: inserted [{}] property with value [{}]", key, value);
                        } else {
                            logger.trace("Importing properties: overwriting previous [{}] property [{}] with [{}]",
                                    key, old, value);
                        }
                    }

                    logger.debug("Imported {} project properties", projectProperties.size());
                    continue;
                }
            }

        }

        return props;
    }

    private boolean parseSingleParamArgumentJoinOldArgs(
            final ParamSwitches givenParam_SWITCHES,
            final String arg,
            final Queue<String> args,
            final Properties props,
            final boolean isLast) {
        final SwitchArgument sa = givenParam_SWITCHES.examine(arg);
        if (sa.isSwitch) {

            final ParamKey argument_KEY = givenParam_SWITCHES.getParamKey();
            final String argumentStringName = givenParam_SWITCHES.getParamKey().toString();

            // parse Target, new arguments are joined with old ones
            logger.trace("Encountered switch [{}]", sa.getSwitch());
            String newArgument;

            if (sa.hasBody()) {
                newArgument = sa.getArgument();
                logger.trace("Parsed " + argumentStringName + "(s) argument [{}]", newArgument);
            } else {
                if (isLast) { throw new IllegalArgumentException(argumentStringName + "(s) cannot be empty!"); }

                newArgument = args.poll();
                logger.trace(argumentStringName + "(s) argument was empty, read next argument [{}]", newArgument);
            }

            final String oldTarget = props.getProperty(argument_KEY.paramKey);
            if (oldTarget != null) {
                logger.trace("Appending old " + argumentStringName + "(s) [{}] to new Target(s) [{}]", oldTarget,
                        newArgument);
                newArgument = oldTarget + "," + newArgument;
            }

            if (newArgument.contains("!")) {
                logger.trace("Overwrite detected, erasing previous arguments ...");
                newArgument = newArgument.replaceFirst(".*!", "");
            }

            logger.debug("Read " + argumentStringName + "(s) argument [{}]", newArgument);
            props.setProperty(argument_KEY.paramKey, newArgument);
            return true;
        }
        return false;
    }

    private boolean parseSingleParamArgumentOwerwriteOldArgs(
            final ParamSwitches givenParam_SWITCHES,
            final String arg,
            final Queue<String> args,
            final Properties properties,
            final boolean isLast) {
        final SwitchArgument sa = givenParam_SWITCHES.examine(arg);
        if (sa.isSwitch) {

            final ParamKey argument_KEY = givenParam_SWITCHES.getParamKey();
            final String argumentStringName = givenParam_SWITCHES.getParamKey().toString();

            // parse the argument, new arguments overwrite old ones
            logger.trace("Encountered switch [{}]", sa.getSwitch());
            String argument_string;

            if (sa.hasBody()) {
                argument_string = sa.getArgument();
                logger.trace("Parsed " + argumentStringName + " argument [{}]", argument_string);
            } else {
                if (isLast) { throw new IllegalArgumentException(argumentStringName + " cannot be empty!"); }

                argument_string = args.poll();
                logger.trace(argumentStringName + " was empty, read next argument [{}]", argument_string);
            }

            if (logger.isTraceEnabled()) {
                final String oldArgument_string = properties.getProperty(argument_KEY.paramKey);
                if (oldArgument_string != null && !oldArgument_string.equals(argument_string)) {
                    logger.trace("Overwriting previous " + argumentStringName + " [{}] with [{}]", oldArgument_string,
                            argument_string);
                }
            }

            logger.debug("Read " + argumentStringName + " argument [{}]", argument_string);
            properties.setProperty(argument_KEY.paramKey, argument_string);
            return true;
        }
        return false;
    }

    private boolean doBooleanFlagArg(
            final ParamSwitches givenParam_SWITCHES,
            final String arg,
            final Queue<String> args,
            final Properties properties) {
        final SwitchArgument sa = givenParam_SWITCHES.examine(arg);
        if (sa.isSwitch) {

            final ParamKey argument_KEY = givenParam_SWITCHES.getParamKey();
            final String argumentStringName = givenParam_SWITCHES.getParamKey().toString();

            // parse the argument, new arguments overwrite old ones
            logger.trace("Encountered switch [{}]", sa.getSwitch());
            String argument_string;

            if (sa.hasBody()) {
                argument_string = sa.getArgument();
                logger.trace("Parsed " + argumentStringName + " argument [{}]", argument_string);
            } else {
                argument_string = "true";
                logger.trace(argumentStringName + " argument was empty, defaulting to [{}]", argument_string);
            }

            if (logger.isTraceEnabled()) {
                final String oldArgument_string = properties.getProperty(argument_KEY.paramKey);
                if (oldArgument_string != null && !oldArgument_string.equals(argument_string)) {
                    logger.trace("Overwriting previous " + argumentStringName + " [{}] with [{}]", oldArgument_string,
                            argument_string);
                }
            }

            logger.debug("Read" + argumentStringName + " argument [{}]", argument_string);
            properties.setProperty(argument_KEY.paramKey, argument_string);

            return true;
        } else return false;
    }
}
