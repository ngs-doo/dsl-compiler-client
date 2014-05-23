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

    private final String inputValue;
    private final String expectedParsedValue;
    private final String switchTestName;
    private final ParamSwitches theActualTestParamSwitch;

    public ParserPassingTests(final String expectedParsedValue
            , final String inputValue
            , final String switchTestName
            , final ParamSwitches theActualTestParamSwitch) {
        this.expectedParsedValue = expectedParsedValue;
        this.inputValue = inputValue;
        this.switchTestName = switchTestName;
        this.theActualTestParamSwitch = theActualTestParamSwitch;
    }

    @Test
    public void testASwitch() throws Exception{
        try{
            /* The switch we are testing, the expected parsed value,
            * input value, and the switch we are testing on
            * are given to us by theTestProvider() */

            final String inputValue = this.inputValue;
            final String expectedParsedValue = this.expectedParsedValue;

            logger.info("========");
            logger.info("The switch we are testing: " + theActualTestParamSwitch);
            logger.info("Input pattern: " + inputValue);
            logger.info("Expected pattern: " + expectedParsedValue);

            /* Tokenize the input pattern */
            final Queue<String> inputPatternQueue = new ArrayDeque<String>();
            for (final String a : inputValue.split("\\|"))
                inputPatternQueue.add(a);

            /* Parse the arguments from the token queue */
            final PropertyLoader propertyLoader =
                    new PropertyLoader(logger, new StreamLoader(logger, new PathExpander(logger)));
            final Arguments arguments =
                    new CachingArgumentsProxy(new ArgumentsValidator(logger,
                            new ArgumentsReader(logger, propertyLoader).readArguments(inputPatternQueue)));

            final String actualPattern = getExpectedOutputString(arguments);
            logger.info("Actual pattern: " + actualPattern);
            logger.info("Asserting for string pair: ("+expectedParsedValue + ", "+actualPattern +")");

            assertEquals(expectedParsedValue, actualPattern);
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

    /**
     * Returns the expected output value of the argument
     */
    private String getExpectedOutputString(final Arguments args){
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
     * Definition for test cases
     *
     * @param inputParameterValue - The input value for the tests' assertion
     * @param shortSwitchVersion - the short name of the switch
     * @param longSwitchVersion - the long name of the switch
     * @param paramSwitch - the {@code ParamSwitches} value, used in getting the actual value of the test.
     *
     * @return A list of test cases that will be fed by the test provider function to the test
     */
    private static List<Object[]> commonTestCases(final String inputParameterValue, final String shortSwitchVersion, final String longSwitchVersion, final ParamSwitches paramSwitch){
        final List<Object[]> testCases = new ArrayList<Object[]>();

        final String expectedParameterValue;

        /* The expected value for target switches is different from the input value*/
        if(paramSwitch.equals(ParamSwitches.TARGET_SWITCHES)){
            final Pattern p = Pattern.compile("^$");

            expectedParameterValue
                = inputParameterValue
                    .replaceAll("^c#", "csharp")
                    .replaceAll("^(csharp|java|scala|php)[-_ ]", "$1_")
                    .replaceAll("^(csharp|java|scala|php)_client", "$1");
        }
        else{
            /* For all other cases, the expected parameter value is equal to the input value */
            expectedParameterValue = inputParameterValue;
        }

        /* The second element of the Object[] array is the input value for the tests. */

        testCases.add(new Object[] { expectedParameterValue, shortSwitchVersion+"|"+inputParameterValue, longSwitchVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, shortSwitchVersion+inputParameterValue, longSwitchVersion, paramSwitch });

        testCases.add(new Object[] { expectedParameterValue, longSwitchVersion+"="+inputParameterValue, longSwitchVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, longSwitchVersion+"|"+inputParameterValue, longSwitchVersion, paramSwitch });

        return testCases;
    }

}
