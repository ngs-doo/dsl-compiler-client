package com.dslplatform.compiler.client.cmdline.parser;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.slf4j.Logger;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.SwitchArgument;

import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.*;
import static com.dslplatform.compiler.client.cmdline.parser.ParamSwitches.*;

public class ArgumentsReader {
    private final Logger logger;
    private final PropertyLoader propertyLoader;

    public ArgumentsReader(
            final Logger logger,
            final PropertyLoader propertyLoader) {
        this.logger = logger;
        this.propertyLoader = propertyLoader;
    }

    public Properties readArguments(final Queue<String> args) throws IOException {
        final Properties properties = new Properties();

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
                        if (isLast) {
                            throw new IllegalArgumentException("ProjectPropertiesPath cannot be empty!");
                        }

                        projectPropertiesPath = args.poll();
                        logger.trace("ProjectPropertiesPath argument was empty, read next argument [{}]", projectPropertiesPath);
                    }

                    logger.debug("Read ProjectPropertiesPath argument [{}]", projectPropertiesPath);
                    final Properties projectProperties = propertyLoader.read(projectPropertiesPath);

                    for (final Map.Entry<Object, Object> entry : projectProperties.entrySet()) {
                        final String key = String.valueOf(entry.getKey());
                        final String value = String.valueOf(entry.getValue());
                        final Object old = properties.setProperty(key, value);

                        if (old == null) {
                            logger.trace("Importing properties: inserted [{}] property with value [{}]", key, value);
                        }
                        else {
                            logger.trace("Importing properties: overwriting previous [{}] property [{}] with [{}]", key, old, value);
                        }
                    }

                    logger.debug("Imported {} project properties", projectProperties.size());
                    continue;
                }
            }

            {
                final SwitchArgument sa = USERNAME_SWITCHES.examine(arg);
                if (sa.isSwitch) {
                    // parse Username, new arguments overwrite old ones
                    logger.trace("Encountered switch [{}]", sa.getSwitch());
                    String username;

                    if (sa.hasBody()) {
                        username = sa.getArgument();
                        logger.trace("Parsed Username argument [{}]", username);
                    } else {
                        if (isLast) {
                            throw new IllegalArgumentException("Username cannot be empty!");
                        }

                        username = args.poll();
                        logger.trace("Username argument was empty, read next argument [{}]", username);
                    }

                    if (logger.isTraceEnabled()) {
                        final String oldUsername = properties.getProperty(USERNAME_KEY.paramKey);
                        if (oldUsername != null && !oldUsername.equals(username)) {
                            logger.trace("Overwriting previous Username [{}] with [{}]", oldUsername, username);
                        }
                    }

                    logger.debug("Read Username argument [{}]", username);
                    properties.setProperty(USERNAME_KEY.paramKey, username);
                    continue;
                }
            }

            {
                final SwitchArgument sa = PROJECT_ID_SWITCHES.examine(arg);
                if (sa.isSwitch) {
                    // parse ProjectID, new arguments overwrite old ones
                    logger.trace("Encountered switch [{}]", sa.getSwitch());
                    String projectID;

                    if (sa.hasBody()) {
                        projectID = sa.getArgument();
                        logger.trace("Parsed ProjectID argument [{}]", projectID);
                    } else {
                        if (isLast) {
                            throw new IllegalArgumentException("ProjectID cannot be empty!");
                        }

                        projectID = args.poll();
                        logger.trace("ProjectID argument was empty, read next argument [{}]", projectID);
                    }

                    if (logger.isTraceEnabled()) {
                        final String oldProjectID = properties.getProperty(PROJECT_ID_KEY.paramKey);
                        if (oldProjectID != null && !oldProjectID.equals(projectID)) {
                            logger.trace("Overwriting previous ProjectID [{}] with [{}]", oldProjectID, projectID);
                        }
                    }

                    logger.debug("Read ProjectID argument [{}]", projectID);
                    properties.setProperty(PROJECT_ID_KEY.paramKey, projectID);
                    continue;
                }
            }

            {
                final SwitchArgument sa = PROJECT_NAME_SWITCHES.examine(arg);
                if (sa.isSwitch) {
                    // parse ProjectName, new arguments overwrite old ones
                    logger.trace("Encountered switch [{}]", sa.getSwitch());
                    String projectName;

                    if (sa.hasBody()) {
                        projectName = sa.getArgument();
                        logger.trace("Parsed ProjectName argument [{}]", projectName);
                    } else {
                        if (isLast) {
                            throw new IllegalArgumentException("ProjectName cannot be empty!");
                        }

                        projectName = args.poll();
                        logger.trace("ProjectName argument was empty, read next argument [{}]", projectName);
                    }

                    if (logger.isTraceEnabled()) {
                        final String oldProjectName = properties.getProperty(PROJECT_NAME_KEY.paramKey);
                        if (oldProjectName != null && !oldProjectName.equals(projectName)) {
                            logger.trace("Overwriting previous ProjectName [{}] with [{}]", oldProjectName, projectName);
                        }
                    }

                    logger.debug("Read ProjectName argument [{}]", projectName);
                    properties.setProperty(PROJECT_NAME_KEY.paramKey, projectName);
                    continue;
                }
            }

            {
                final SwitchArgument sa = PACKAGE_NAME_SWITCHES.examine(arg);
                if (sa.isSwitch) {
                    // parse PackageName, new arguments overwrite old ones
                    logger.trace("Encountered switch [{}]", sa.getSwitch());
                    String projectName;

                    if (sa.hasBody()) {
                        projectName = sa.getArgument();
                        logger.trace("Parsed PackageName argument [{}]", projectName);
                    } else {
                        if (isLast) {
                            throw new IllegalArgumentException("PackageName cannot be empty!");
                        }

                        projectName = args.poll();
                        logger.trace("PackageName argument was empty, read next argument [{}]", projectName);
                    }

                    if (logger.isTraceEnabled()) {
                        final String oldPackageName = properties.getProperty(PACKAGE_NAME_KEY.paramKey);
                        if (oldPackageName != null && !oldPackageName.equals(projectName)) {
                            logger.trace("Overwriting previous PackageName [{}] with [{}]", oldPackageName, projectName);
                        }
                    }

                    logger.debug("Read PackageName argument [{}]", projectName);
                    properties.setProperty(PACKAGE_NAME_KEY.paramKey, projectName);
                    continue;
                }
            }

            {
                final SwitchArgument sa = TARGET_SWITCHES.examine(arg);
                if (sa.isSwitch) {
                    // parse Target, new arguments are joined with old ones
                    logger.trace("Encountered switch [{}]", sa.getSwitch());
                    String target;

                    if (sa.hasBody()) {
                        target = sa.getArgument();
                        logger.trace("Parsed Target(s) argument [{}]", target);
                    } else {
                        if (isLast) {
                            throw new IllegalArgumentException("Target(s) cannot be empty!");
                        }

                        target = args.poll();
                        logger.trace("Target(s) argument was empty, read next argument [{}]", target);
                    }

                    final String oldTarget = properties.getProperty(TARGET_KEY.paramKey);
                    if (oldTarget != null) {
                        logger.trace("Appending old Target(s) [{}] to new Target(s) [{}]", oldTarget, target);
                        target = oldTarget + "," + target;
                    }

                    if (target.contains("!")) {
                        logger.trace("Overwrite detected, erasing previous targets ...");
                        target = target.replaceFirst(".*!", "");
                    }

                    logger.debug("Read Target(s) argument [{}]", target);
                    properties.setProperty(TARGET_KEY.paramKey, target);
                    continue;
                }
            }

            {
                final SwitchArgument sa = WITH_ACTIVE_RECORD_SWITCHES.examine(arg);
                if (sa.isSwitch) {
                    // parse WithActiveRecord, new arguments overwrite old ones
                    logger.trace("Encountered switch [{}]", sa.getSwitch());
                    String withActiveRecord;

                    if (sa.hasBody()) {
                        withActiveRecord = sa.getArgument();
                        logger.trace("Parsed WithActiveRecord argument [{}]", withActiveRecord);
                    } else {
                        withActiveRecord = "true";
                        logger.trace("WithActiveRecord argument was empty, defaulting to [{}]", withActiveRecord);
                    }

                    if (logger.isTraceEnabled()) {
                        final String oldWithActiveRecord = properties.getProperty(WITH_ACTIVE_RECORD_KEY.paramKey);
                        if (oldWithActiveRecord != null && !oldWithActiveRecord.equals(withActiveRecord)) {
                            logger.trace("Overwriting previous WithActiveRecord [{}] with [{}]", oldWithActiveRecord, withActiveRecord);
                        }
                    }

                    logger.debug("Read WithActiveRecord argument [{}]", withActiveRecord);
                    properties.setProperty(WITH_ACTIVE_RECORD_KEY.paramKey, withActiveRecord);
                    continue;
                }
            }
        }

        return properties;
    }
}
