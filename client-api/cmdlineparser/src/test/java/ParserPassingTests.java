import static org.junit.Assert.assertEquals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Pattern;

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
import com.dslplatform.compiler.client.cmdline.parser.ParamSwitches;
import com.dslplatform.compiler.client.params.LoggingLevel;
import com.dslplatform.compiler.client.params.Target;
import com.dslplatform.compiler.client.util.PathExpander;

@RunWith(Parameterized.class)
public class ParserPassingTests {

    final Logger logger = LoggerFactory.getLogger("testni kekec");

    private final String inputPattern;
    private final String expectedPattern;
    private final String switchTestName;
    private final ParamSwitches theActualTestParamSwitch;

    public ParserPassingTests(final String expectedPattern
            , final String inputPattern
            , final String switchTestName
            , final ParamSwitches theActualTestParamSwitch) {
        this.expectedPattern = expectedPattern;
        this.inputPattern = inputPattern;
        this.switchTestName = switchTestName;
        this.theActualTestParamSwitch = theActualTestParamSwitch;
    }

    @Test
    public void parseOutputPathTest() throws Exception{
        try{
        final String inputPattern = this.inputPattern;
        final String expectedPattern = this.expectedPattern;

        logger.info("========");
        logger.info("The switch we are testing: " + theActualTestParamSwitch);
        logger.info("Input pattern: " + inputPattern);
        logger.info("Expected pattern: " + expectedPattern);

        final Queue<String> inputPatternQueue = new ArrayDeque<String>();
        for (final String a : inputPattern.split("\\|"))
            inputPatternQueue.add(a);

        final PropertyLoader propertyLoader =
                new PropertyLoader(logger, new StreamLoader(logger, new PathExpander(logger)));

        final Arguments arguments =
                new CachingArgumentsProxy(new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(inputPatternQueue)));

        final String actualPattern = getOutputString(arguments);
        logger.info("Actual pattern: " + actualPattern);
        logger.info("Asserting for string pair: ("+expectedPattern + ", "+actualPattern +")");

        /* For Target c# must be matched as csharp, and ${language}_client is always ${language} */

        final Pattern targetPattern = Pattern.compile("^(c#|csharp|java|scala|php)([-_ ](client|server|portable))?$", Pattern.CASE_INSENSITIVE);

        if(targetPattern.matcher(expectedPattern).matches()){
            final String expected = expectedPattern
                   .replaceAll("^(c#|csharp|java|scala|php)[-_ ](client|server|portable)$", "$1_$2")
                   .replaceAll("_client$", "")
                   .replace("c#", "csharp");

            assertEquals(expected, actualPattern);
        }
        else{
            assertEquals(expectedPattern, actualPattern);
        }
     }
        catch(final Exception e){
            logger.info("Exception: " + e.getMessage());
            throw (e);
        }

    }

    @Parameterized.Parameters(name = "Testing for switch:  {2}  on single pattern: {0}")
    public static Iterable<Object[]> theTestProvider() {
        final List<Object[]> inputPatterns = new ArrayList<Object[]>();

        for(final ParamSwitches paramSwitch : ParamSwitches.values()){
            /* Skip the PROJECT_PROPS_PATH_SWITCHES, it's tested elsewhere */
            if(paramSwitch.equals(ParamSwitches.PROJECT_PROPS_PATH_SWITCHES))
                continue;

            inputPatterns.addAll(inputPatternsForParamSwitch(paramSwitch));
        }

        return inputPatterns;
    }

    private static List<Object[]> inputPatternsForParamSwitch(final ParamSwitches paramSwitch){
        final List<Object[]> inputPatterns = new ArrayList<Object[]>();

        final List<String> switches = Arrays.asList(paramSwitch.getSwitches());

        final String parameterValue = "some_parameter.val";
        final String parameterValueWithEscapedSpaces = "some\\ parameter\\ val";

        if(paramSwitch.equals(ParamSwitches.HELP) || paramSwitch.equals(ParamSwitches.END_OF_PARAMS)){
            /* Not really switches, so we compare nothing */
            final String shortVersion = switches.get(0);

            inputPatterns.add(new Object[] { Boolean.toString(true), shortVersion, shortVersion, paramSwitch});

            if(switches.size() > 1){
                final String longVersion = switches.get(1);
                inputPatterns.add(new Object[] { Boolean.toString(true), longVersion, shortVersion, paramSwitch});
            }
        }
        else if (paramSwitch.equals(ParamSwitches.TARGET_SWITCHES)){
            /* We test for each value of the enum */

            final String shortVersion = switches.get(0);
            final String longVersion = switches.get(1);

            for (final Target target : Target.values()){
                final String targetParamVal = target.targetName;
                inputPatterns.addAll(commonTestCases(targetParamVal, shortVersion, longVersion, paramSwitch));
            }

            final String[] targetStringPatterns = new String[]{
                    "c#", "csharp","java","php","scala"
                    ,"c# client","csharp client","java client","php client","scala client"
                    ,"c#_client","csharp_client","java_client","php_client","scala_client"
                    ,"c#-client","csharp-client","java-client","php-client","scala-client"

                    ,"c# portable","csharp portable"
                    ,"c#_portable","csharp_portable"
                    ,"c#-portable","csharp-portable"

                    ,"c# server","csharp server","scala server"
                    ,"c#_server","csharp_server","scala_server"
                    ,"c#-server","csharp-server","scala-server"
                    };

            for(final String pattern : targetStringPatterns){
                inputPatterns.addAll(commonTestCases(pattern, shortVersion, longVersion, paramSwitch));
            }

        }
        else if (paramSwitch.equals(ParamSwitches.LOGGING_LEVEL_SWITCHES)){
            /* We test for each value of the enum */

            final String shortVersion = switches.get(0);
            final String longVersion = switches.get(1);

            for (final LoggingLevel loggingLevel : LoggingLevel.values()){
                final String loggingParameterValue = loggingLevel.level;

                inputPatterns.addAll(commonTestCases(loggingParameterValue, shortVersion, longVersion, paramSwitch));

            }

        }
        else if(paramSwitch.equals(ParamSwitches.PROJECT_NAME_SWITCHES)){
            // TODO: wrong

            final String shortVersion = switches.get(0);
            final String longVersion = switches.get(1);

            inputPatterns.addAll(commonTestCases(parameterValue, shortVersion, longVersion, paramSwitch));

        }
        else if(paramSwitch.equals(ParamSwitches.PROJECT_ID_SWITCHES)){

            final String shortVersion = switches.get(0);
            final String longVersion = switches.get(1);

            final String projectIdParamVal = UUID.fromString("1-2-3-4-5").toString();

            inputPatterns.addAll(commonTestCases(projectIdParamVal, shortVersion, longVersion, paramSwitch));
        }
        else if(switches.size() > 1)
        {
            /* It's a single property value*/

            final String shortVersion = switches.get(0);
            final String longVersion = switches.get(1);

            inputPatterns.addAll(commonTestCases(parameterValue, shortVersion, longVersion, paramSwitch));
            inputPatterns.add(new Object[] { parameterValueWithEscapedSpaces, longVersion + "=" + parameterValueWithEscapedSpaces, longVersion, paramSwitch});
        }
        else if(paramSwitch.equals(ParamSwitches.CACHE_PATH_SWITCHES)){

            /* No short version, but not a flag */

            final String longVersion = switches.get(0);

            //inputPatterns.addAll(commonTestCases(parameterValue, longVersion, longVersion, paramSwitch));
            inputPatterns.add(new Object[] { parameterValueWithEscapedSpaces, longVersion + "=" + parameterValueWithEscapedSpaces, longVersion, paramSwitch});
            inputPatterns.add(new Object[] { parameterValueWithEscapedSpaces, longVersion + "|" + parameterValueWithEscapedSpaces, longVersion, paramSwitch});
        }
        else{
            /* It's a flag */
            final String shortVersion = switches.get(0);
            inputPatterns.add(new Object[] { Boolean.toString(true), shortVersion, shortVersion, paramSwitch});
        }

        return inputPatterns;
    }

    private String getOutputString(final Arguments args){
        switch(theActualTestParamSwitch){
            case END_OF_PARAMS:
            case HELP:
                return Boolean.toString(true);
            case LOGGING_LEVEL_SWITCHES:
                return args.getLoggingLevel().level;
            case OUTPUT_PATH_SWITCHES:
                return args.getOutputPath().outputPath.getName();
            case CACHE_PATH_SWITCHES:
                return args.getCachePath().cachePath.getName();
            case USERNAME_SWITCHES:
                return args.getUsername().username;
            case PROJECT_ID_SWITCHES:
                return args.getProjectID().projectID.toString();
            case PROJECT_NAME_SWITCHES:
                return args.getProjectName().projectName;
            case PACKAGE_NAME_SWITCHES:
                return args.getPackageName().packageName;
            case TARGET_SWITCHES:
                return args.getTargets().getTargetSet().toArray(new Target[0])[0].targetName;
            case WITH_ACTIVE_RECORD_SWITCHES:
                return Boolean.toString(args.isWithActiveRecord());
            case WITH_HELPER_METHODS_SWITCHES:
                return Boolean.toString(args.isWithActiveRecord());
            case WITH_JACKSON_SWITCHES:
                return Boolean.toString(args.isWithJackson());
            case WITH_JAVA_BEANS_SWITCHES:
                return Boolean.toString(args.isWithJavaBeans());
            case SKIP_DIFF_SWITCHES:
                return Boolean.toString(args.isSkipDiff());
            case ALLOW_UNSAFE_SWITCHES:
                return Boolean.toString(args.isAllowUnsafe());

            default:
                return null;
        }

    }

    /**
     * Here at last we define test cases.
     *
     * @param expectedParameterValue - The expected value for the tests' assertion
     * @param shortVersion - the short name of the switch
     * @param longVersion - the long name of the switch
     * @param paramSwitch - the {@code ParamSwitches} value, used in getting the actual value of the test.
     * @return
     */
    private static List<Object[]> commonTestCases(final String expectedParameterValue, final String shortVersion, final String longVersion, final ParamSwitches paramSwitch){
        final List<Object[]> testCases = new ArrayList<Object[]>();

        /* The second element of the Object[] array is the input value for the tests. */

        testCases.add(new Object[] { expectedParameterValue, shortVersion+"|"+expectedParameterValue, longVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, shortVersion+expectedParameterValue, longVersion, paramSwitch });

        testCases.add(new Object[] { expectedParameterValue, longVersion+"="+expectedParameterValue, longVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, longVersion+"|"+expectedParameterValue, longVersion, paramSwitch });

      //inputPatterns.add(new Object[] { "/home/username/"+parameterValue, shortVersion +"="+"~"+parameterValue, shortVersion, paramSwitch});
        //inputPatterns.add(new Object[] { parameterValueWithEscapedSpaces, longVersion + "=" + parameterValueWithEscapedSpaces, longVersion, paramSwitch});

        return testCases;
    }

}
