package com.dslplatform.compiler.client.cmdline.parser;
import static org.junit.Assert.assertEquals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.params.LoggingLevel;
import com.dslplatform.compiler.client.io.PathExpander;

@RunWith(Parameterized.class)
public class SingleParametersTests {

    final Logger logger = LoggerFactory.getLogger(SingleParametersTests.class);

    private final String inputValue;
    private final String expectedParsedValue;
    private final String switchTestName;
    private final ParamSwitches theActualTestParamSwitch;

    public SingleParametersTests(final String expectedParsedValue
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
                    new CachingArgumentsProxy(logger, new ArgumentsValidator(logger,
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

        final Set<ParamSwitches> singleParameterSwitches = EnumSet.of(
                ParamSwitches.END_OF_PARAMS
                , ParamSwitches.HELP
                , ParamSwitches.USERNAME_SWITCHES
                , ParamSwitches.OUTPUT_PATH_SWITCHES
                , ParamSwitches.CACHE_PATH_SWITCHES
                , ParamSwitches.LOGGING_LEVEL_SWITCHES
                , ParamSwitches.PROJECT_NAME_SWITCHES
                , ParamSwitches.PACKAGE_NAME_SWITCHES
                , ParamSwitches.PROJECT_ID_SWITCHES
                );

        for(final ParamSwitches paramSwitch : singleParameterSwitches){
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

        final String expectedParameterValue = inputParameterValue;

        /* The second element of the Object[] array is the input value for the tests. */

        testCases.add(new Object[] { expectedParameterValue, shortSwitchVersion+"|"+inputParameterValue, longSwitchVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, shortSwitchVersion+inputParameterValue, longSwitchVersion, paramSwitch });

        testCases.add(new Object[] { expectedParameterValue, longSwitchVersion+"="+inputParameterValue, longSwitchVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, longSwitchVersion+"|"+inputParameterValue, longSwitchVersion, paramSwitch });

        return testCases;
    }

}
