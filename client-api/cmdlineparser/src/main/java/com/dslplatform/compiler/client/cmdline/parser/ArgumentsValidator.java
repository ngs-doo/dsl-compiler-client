package com.dslplatform.compiler.client.cmdline.parser;

import static com.dslplatform.compiler.client.cmdline.parser.ParamDefaults.*;
import static com.dslplatform.compiler.client.cmdline.parser.ParamKey.*;

import java.util.EnumSet;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;

import com.dslplatform.compiler.client.params.*;

public class ArgumentsValidator implements Arguments {
    private final Logger logger;
    private final Properties properties;

    public ArgumentsValidator(
            final Logger logger,
            final Properties properties) {
        this.logger = logger;
        this.properties = properties;
    }

    @Override
    public Username getUsername() {
        final String username = properties.getProperty(USERNAME_KEY.paramKey);
        logger.trace("Validating Username [{}] ...", username);
        if (username == null) throw new IllegalArgumentException("Username was not defined!");
        final Username result = new Username(username);
        logger.debug("Retrieved Username from the properties [{}]", result);
        return result;
    }

    @Override
    public ProjectID getProjectID() {
        final String projectID = properties.getProperty(PROJECT_ID_KEY.paramKey);
        logger.trace("Validating ProjectID [{}] ...", projectID);
        if (projectID == null) throw new IllegalArgumentException("ProjectID was not defined!");
        final ProjectID result;
        try {
            result = new ProjectID(UUID.fromString(projectID));
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" does not look like a ProjectID!", projectID));
        }
        logger.debug("Retrieved ProjectID from the properties [{}]", result);
        return result;
    }

    @Override
    public ProjectName getProjectName() {
        final String projectName = properties.getProperty(PROJECT_NAME_KEY.paramKey);
        logger.trace("Validating ProjectName [{}] ...", projectName);
        if (projectName == null) throw new IllegalArgumentException("ProjectName was not defined!");
        final ProjectName result = new ProjectName(projectName);
        logger.debug("Retrieved ProjectName from the properties [{}]", result);
        return result;
    }

    @Override
    public PackageName getPackageName() {
        String packageName = properties.getProperty(PACKAGE_NAME_KEY.paramKey);
        logger.trace("Validating PackageName [{}] ...", packageName);
        if (packageName == null) {
            packageName = PACKAGE_NAME_DEFAULT.defaultValue;
            logger.trace("PackageName was not defined, defaulting to [{}]", packageName);
        }
        final PackageName result = new PackageName(packageName);
        logger.debug("Retrieved PackageName from the properties [{}]", result);
        return result;
    }

    @Override
    public Targets getTargets() {
        String target = properties.getProperty(TARGET_KEY.paramKey);
        logger.trace("Validating Target(s) [{}] ...", target);
        if (target == null) {
            target = TARGET_DEFAULT.defaultValue;
            logger.trace("Target(s) were not defined, defaulting to [{}]", target);
        }

        final EnumSet<Target> targetSet = EnumSet.noneOf(Target.class);
        for (final String currentTarget : target.split("\\s*,+\\s*")) {
            logger.trace("Validating Target [{}] ...", currentTarget);
            final Target foundTarget = Target.find(currentTarget);
            if (foundTarget == null) throw new IllegalArgumentException(
                    "Target [" + currentTarget + "] does not exist, valid targets are: " + Target.getValidTargets());

            targetSet.add(foundTarget);
        }

        final Targets result = new Targets(targetSet);
        logger.debug("Retrieved Target(s) from the properties [{}]", result);
        return result;
    }

    @Override
    public boolean isWithActiveRecord() {
        String withActiveRecord = properties.getProperty(WITH_ACTIVE_RECORD_KEY.paramKey);
        if (withActiveRecord == null) {
            withActiveRecord = WITH_ACTIVE_RECORD_DEFAULT.defaultValue;
            logger.trace("WithActiveRecord was not defined, defaulting to [{}]", withActiveRecord);
        }
        final boolean result = booleanValue(withActiveRecord);
        logger.debug("Retrieved WithActiveRecord from properties [{}]", result);
        return result;
    }

    private static boolean booleanValue(final String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1")) return true;
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equals("0")) return false;

        throw new IllegalArgumentException("Illegal boolean value [" + value + "], allowed values are true/1/yes and false/0/no");
    }
}
