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
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsReader;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsValidator;
import com.dslplatform.compiler.client.cmdline.parser.CachingArgumentsProxy;
import com.dslplatform.compiler.client.params.Target;
import com.dslplatform.compiler.client.util.PathExpander;

@RunWith(Parameterized.class)
public class ProjectPropsPathPassingTests {

    final Logger logger = LoggerFactory.getLogger(ProjectPropsPathPassingTests.class);

    private final String inputPattern;

    public ProjectPropsPathPassingTests(final String inputPattern) {
        this.inputPattern = inputPattern;
    }

    @Parameterized.Parameters(name = "Testing on pattern: {0}")
    public static Iterable<Object[]> theTestProvider() {
        final List<Object[]> inputPatterns = new ArrayList<Object[]>();


            inputPatterns.add(new Object[]{"-f|src/test/resources/example.props"});
            inputPatterns.add(new Object[]{"--project-properties-path=src/test/resources/example.props"});
            inputPatterns.add(new Object[]{"--project-properties-path|src/test/resources/example.props"});


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
                new CachingArgumentsProxy(new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(inputPatternQueue)));

        assertEquals("knuckles-the-echidna@dsl-platform.com",arguments.getUsername().username);
        assertEquals(UUID.fromString("1-2-3-4-5").toString(),arguments.getProjectID().projectID.toString());
        assertEquals("RedEchidna",arguments.getProjectName().projectName);
        assertEquals("DEBUG",arguments.getLoggingLevel().level);
        assertEquals("~/knuckles/workspace/outputPath",arguments.getOutputPath().outputPath.getPath());
        assertEquals("com.dslplatform.knučkles.foo",arguments.getPackageName().packageName);

        assertEquals(
                arguments.getTargets().getTargetSet(),
                EnumSet.of(
                        Target.JAVA_CLIENT
                        , Target.CSHARP_CLIENT
                        , Target.CSHARP_PORTABLE
                        , Target.CSHARP_SERVER
                        , Target.PHP_CLIENT
                        , Target.SCALA_CLIENT
                        , Target.SCALA_SERVER));


    }

}
