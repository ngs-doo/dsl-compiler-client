package com.dslplatform.compiler.client.cmdline.parser;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.io.PathExpander;
import com.dslplatform.compiler.client.params.Action;
import com.dslplatform.compiler.client.params.Target;

@RunWith(Parameterized.class)
public class ProjectPropsSwitchTests {

    final Logger logger = LoggerFactory.getLogger(ProjectPropsSwitchTests.class);

    private final String inputPattern;

    public ProjectPropsSwitchTests(final String inputPattern) {
        this.inputPattern = inputPattern;
    }

    @Parameterized.Parameters(name = "Testing on pattern: {0}")
    public static Iterable<Object[]> theTestProvider() {
        final List<Object[]> inputPatterns = new ArrayList<Object[]>();

        inputPatterns.add(new Object[] { "-f|src/test/resources/example.props" });
        inputPatterns.add(new Object[] { "--project-properties-path=src/test/resources/example.props" });
        inputPatterns.add(new Object[] { "--project-properties-path|src/test/resources/example.props" });

        return inputPatterns;
    }

    @Test
    public void parseProjectPropsPathTest() throws IOException {
        logger.info("========");
        logger.info("Testing the project-properties-path switch:");

        final Queue<String> inputPatternQueue = new ArrayDeque<String>();

        for(final String inputPatternElement : this.inputPattern.split("\\|")){
            inputPatternQueue.add(inputPatternElement);
        }

        logger.info(inputPatternQueue.toString());

        final PropertyLoader propertyLoader =
                new PropertyLoader(logger, new StreamLoader(logger, new PathExpander(logger)));

        final Arguments arguments =
                new CachingArgumentsProxy(logger, new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(inputPatternQueue)));

        assertEquals("RedEchidna",arguments.getProjectName().projectName);
        assertEquals("knuckles-the-echidna@dsl-platform.com",arguments.getUsername().username);
        assertEquals("m15m13p0713p0p4s3l",arguments.getPassword().password);
        assertEquals(UUID.fromString("1-2-3-4-5").toString(),arguments.getProjectID().projectID.toString());
        // todo: api-url
        assertEquals("com.dslplatform.knučkles.foo",arguments.getPackageName().packageName);
        assertEquals("DEBUG",arguments.getLoggingLevel().level);
        assertEquals("~/knuckles/workspace/outputPath",arguments.getOutputPath().outputPath.getPath());
        assertEquals("~/knuckles/workspace/dslPath",arguments.getDSLPath().dslPath.getPath());
        assertEquals("/knuckles/workspace/migrationPath",arguments.getMigrationFilePath().migrationFilePath.getPath());
        assertEquals("aUsername",arguments.getDBAuth().getDbUsername().dbUsername);
        assertEquals("anPassword",arguments.getDBAuth().getDbPassword().dbPassword);
        assertEquals("localhost",arguments.getDBAuth().getDbHost().dbHost);
        assertEquals(true, 5432==arguments.getDBAuth().getDbPort().dbPort);
        assertEquals("dbname",arguments.getDBAuth().getDbDatabaseName().dbDatabaseName);

        assertEquals("dbconnstring",arguments.getDBAuth().getDbConnectionString().dbConnectionString);

        assertEquals(
                arguments.getActions().getActionSet(),
                EnumSet.of(Action.UPDATE
                        , Action.CONFIG
                        , Action.PARSE
                        , Action.GET_CHANGES
                        , Action.LAST_DSL
                        , Action.GENERATE_SOURCES
                        , Action.DOWNLOAD_GENERATED_MODEL
                        , Action.UNMANAGED_SQL_MIGRATION
                        ));

        assertEquals(
                arguments.getTargets().getTargetSet(),
                EnumSet.of(Target.JAVA_CLIENT
                        , Target.CSHARP_CLIENT
                        , Target.CSHARP_PORTABLE
                        , Target.CSHARP_SERVER
                        , Target.PHP_CLIENT
                        , Target.SCALA_CLIENT
                        , Target.SCALA_SERVER));
    }

}
