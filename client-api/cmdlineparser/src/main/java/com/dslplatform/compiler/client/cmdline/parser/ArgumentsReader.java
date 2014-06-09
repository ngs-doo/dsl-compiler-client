package com.dslplatform.compiler.client.cmdline.parser;

import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.ALLOW_UNSAFE_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.CACHE_PATH_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.END_OF_PARAMS;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.LOGGING_LEVEL_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.OUTPUT_PATH_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.PACKAGE_NAME_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.PROJECT_ID_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.PROJECT_NAME_SWITCHES;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.PROJECT_PROPS_PATH_SWITCHES;
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
            final String current_arg = args.peek();

            if (current_arg == null) {
                logger.trace("Stopping argument reader (end of argument list)");
                break;
            }

            if (END_OF_PARAMS.is(current_arg)) {
                logger.debug("Stopping argument reader (encountered end-of-params marker)");
                break;
            }

            args.poll();
            logger.trace("Read argument: {}", current_arg);

            final boolean isLast = args.isEmpty();
            if (isLast) logger.trace("This is the last argument ...");

            /* --- Switches --- */

            /* Parameters that overwrite previous values */
            if (ifSwitchType_doParseOwerwriteOld(USERNAME_SWITCHES, current_arg, args, props, isLast)) continue;
            if (ifSwitchType_doParseOwerwriteOld(OUTPUT_PATH_SWITCHES, current_arg, args, props, isLast)) continue;
            if (ifSwitchType_doParseOwerwriteOld(CACHE_PATH_SWITCHES, current_arg, args, props, isLast)) continue;
            if (ifSwitchType_doParseOwerwriteOld(LOGGING_LEVEL_SWITCHES, current_arg, args, props, isLast)) continue;
            if (ifSwitchType_doParseOwerwriteOld(PROJECT_NAME_SWITCHES, current_arg, args, props, isLast)) continue;
            if (ifSwitchType_doParseOwerwriteOld(PACKAGE_NAME_SWITCHES, current_arg, args, props, isLast)) continue;
            if (ifSwitchType_doParseOwerwriteOld(PROJECT_ID_SWITCHES, current_arg, args, props, isLast)) continue;

            /* Parameters that join new values to the old ones */
            if (ifSwitchType_doParseJoinOld(TARGET_SWITCHES, current_arg, args, props, isLast)) continue;

            /* Boolean flag switches: */
            if (ifSwitchType_doParseFlag(WITH_ACTIVE_RECORD_SWITCHES, current_arg, args, props)) continue;
            if (ifSwitchType_doParseFlag(WITH_JAVA_BEANS_SWITCHES, current_arg, args, props)) continue;
            if (ifSwitchType_doParseFlag(WITH_JACKSON_SWITCHES, current_arg, args, props)) continue;
            if (ifSwitchType_doParseFlag(WITH_HELPER_METHODS_SWITCHES, current_arg, args, props)) continue;
            if (ifSwitchType_doParseFlag(SKIP_DIFF_SWITCHES, current_arg, args, props)) continue;
            if (ifSwitchType_doParseFlag(ALLOW_UNSAFE_SWITCHES, current_arg, args, props)) continue;

            /* Load properties from file: */
            if(ifPropsPath_doParsePropsFromPath(current_arg, args, props, isLast)) continue;

            /* --- Actions: --- */
            // (Everything else is an action)
            if(processedAction(Action.valueOf(current_arg))) continue;

            logger.error("Invalid argument token: " + current_arg);
        }

        return props;
    }

    private boolean processedAction(final Action action){

        return false;
    }

    /**
     * If {@code current_arg} is of type {@code PROJECT_PROPS_PATH_SWITCHES} processes it.
     *
     * @return true {@code current_arg} is of {@code switchType}
     * @return true {@code current_arg} is not of {@code switchType}
     *
     * @throws IOException if loading of the props file failed.
     */
    private boolean ifPropsPath_doParsePropsFromPath(
            final String current_arg,
            final Queue<String> args,
            final Properties props,
            final boolean isLast
            )throws IOException{

        final SwitchArgument switchArgument = PROJECT_PROPS_PATH_SWITCHES.examine(current_arg);
        if (switchArgument.isSwitch) {
            // parse ProjectPropertiesPath, new arguments overwrite old ones
            logger.trace("Encountered switch [{}]", switchArgument.getSwitch());
            String projectPropertiesPath;

            if (switchArgument.hasBody()) {
                projectPropertiesPath = switchArgument.getArgument();
                logger.trace("Parsed ProjectPropertiesPath argument [{}]", projectPropertiesPath);
            } else {
                if (isLast) { throw new IllegalArgumentException("ProjectPropertiesPath cannot be empty!"); }

                projectPropertiesPath = args.poll();
                logger.trace("ProjectPropertgetiesPath argument was empty, read next argument [{}]",
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
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * If {@code current_arg} is of {@code switchType} processes it.
     *
     *  @return true {@code current_arg} is of {@code switchType}
     *  @return true {@code current_arg} is not of {@code switchType}
     */
    private boolean ifSwitchType_doParseJoinOld(
            final ParamSwitches switchType,
            final String current_arg,
            final Queue<String> args,
            final Properties props,
            final boolean isLast) {
        final SwitchArgument switchArgument = switchType.examine(current_arg);
        if (switchArgument.isSwitch) {

            final ParamKey argument_KEY = switchType.getParamKey();
            final String argumentStringName = switchType.getParamKey().toString();

            // parse Target, new arguments are joined with old ones
            logger.trace("Encountered switch [{}]", switchArgument.getSwitch());
            String newArgument;

            if (switchArgument.hasBody()) {
                newArgument = switchArgument.getArgument();
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
        else{
            return false;
        }
    }

    /**
     * If {@code current_arg} is of {@code switchType} processes it.
     *
     *  @return true {@code current_arg} is of {@code switchType}
     *  @return true {@code current_arg} is not of {@code switchType}
     */
    private boolean ifSwitchType_doParseOwerwriteOld(
            final ParamSwitches switchType,
            final String current_arg,
            final Queue<String> args,
            final Properties properties,
            final boolean isLast) {
        final SwitchArgument switchArgument = switchType.examine(current_arg);
        if (switchArgument.isSwitch) {

            final ParamKey argumentKey = switchType.getParamKey();
            final String argumentKey_stringName = switchType.getParamKey().toString();

            // parse the argument, new arguments overwrite old ones
            logger.trace("Encountered switch [{}]", switchArgument.getSwitch());
            String argument_string;

            if (switchArgument.hasBody()) {
                argument_string = switchArgument.getArgument();
                logger.trace("Parsed " + argumentKey_stringName + " argument [{}]", argument_string);
            } else {
                if (isLast) { throw new IllegalArgumentException(argumentKey_stringName + " cannot be empty!"); }

                argument_string = args.poll();
                logger.trace(argumentKey_stringName + " was empty, read next argument [{}]", argument_string);
            }

            if (logger.isTraceEnabled()) {
                final String oldArgument_string = properties.getProperty(argumentKey.paramKey);
                if (oldArgument_string != null && !oldArgument_string.equals(argument_string)) {
                    logger.trace("Overwriting previous " + argumentKey_stringName + " [{}] with [{}]", oldArgument_string,
                            argument_string);
                }
            }

            logger.debug("Read " + argumentKey_stringName + " argument [{}]", argument_string);
            properties.setProperty(argumentKey.paramKey, argument_string);
            return true;
        }
        else{
            return false;
        }
    }

    /**
     *  If {@code current_arg} is of {@code switchType} processes it.
     *
     *  @return true {@code current_arg} is of {@code switchType}
     *  @return true {@code current_arg} is not of {@code switchType}
     */
    private boolean ifSwitchType_doParseFlag(
            final ParamSwitches switchType,
            final String current_arg,
            final Queue<String> args,
            final Properties properties) {

        final SwitchArgument switchArgument = switchType.examine(current_arg);
        if (switchArgument.isSwitch) {

            final ParamKey argument_KEY = switchType.getParamKey();
            final String argumentStringName = switchType.getParamKey().toString();

            // parse the argument, new arguments overwrite old ones
            logger.trace("Encountered switch [{}]", switchArgument.getSwitch());
            String argument_string;

            if (switchArgument.hasBody()) {
                argument_string = switchArgument.getArgument();
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
        } else {
            return false;
        }
    }
}
