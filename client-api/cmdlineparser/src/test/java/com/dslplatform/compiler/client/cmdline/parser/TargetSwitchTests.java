package com.dslplatform.compiler.client.cmdline.parser;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.params.Target;
import com.dslplatform.compiler.client.io.PathExpander;

@RunWith(Parameterized.class)
public class TargetSwitchTests {

    final Logger logger = LoggerFactory.getLogger(TargetSwitchTests.class);

    private final String inputValue;
    private final String expectedParsedValue;
    private final String switchTestName;
    private final ParamSwitches theActualTestParamSwitch;

    public TargetSwitchTests(final String expectedParsedValue
            , final String inputValue
            , final String switchTestName
            , final ParamSwitches theActualTestParamSwitch) {
        this.expectedParsedValue = expectedParsedValue;
        this.inputValue = inputValue;
        this.switchTestName = switchTestName;
        this.theActualTestParamSwitch = theActualTestParamSwitch;
    }

    @Test
    public void testTargetSwitches() throws Exception{
        try{
            /* The switch we are testing, the expected parsed value,
            * input value, and the switch we are testing on
            * are given to us by theTestProvider() */

            final String inputValue = this.inputValue;
            final String expectedParsedValue = this.expectedParsedValue;

            logger.info("========");
            logger.info("The switch we are testing: " + theActualTestParamSwitch);
            logger.info("Input pattern: " + inputValue);
            logger.info("Expected that the target set contains: " + expectedParsedValue);

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

            logger.info("Asserting that the target set contains a value for: "+expectedParsedValue);

            assertTrue(arguments.getTargets().getTargetSet().contains(Target.find(expectedParsedValue)));
        }
        catch(final Exception e){
            logger.info("Exception: " + e.getMessage());
            throw (e);
        }

    }

    @Parameterized.Parameters(name = "Testing for switch:  {2}  on single pattern: {0}")
    public static Iterable<Object[]> theTestProvider() {
        final List<Object[]> inputPatterns = new ArrayList<Object[]>();

        final String[] switches = ParamSwitches.TARGET_SWITCHES.getSwitches();
        final String shortVersion = switches[0];
        final String longVersion = switches[1];


        for (final Target target : Target.values()){
            final String targetParamVal = target.targetName;
            inputPatterns.addAll(commonTestCases(targetParamVal, shortVersion, longVersion, ParamSwitches.TARGET_SWITCHES));
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
            inputPatterns.addAll(commonTestCases(pattern, shortVersion, longVersion, ParamSwitches.TARGET_SWITCHES));
        }

        return inputPatterns;
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
            final Pattern p = Pattern.compile("^$");

            expectedParameterValue
                = inputParameterValue
                    .replaceAll("^c#", "csharp")
                    .replaceAll("^(csharp|java|scala|php)[-_ ]", "$1_")
                    .replaceAll("^(csharp|java|scala|php)_client", "$1");

        /* The second element of the Object[] array is the input value for the tests. */

        testCases.add(new Object[] { expectedParameterValue, shortSwitchVersion+"|"+inputParameterValue, longSwitchVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, shortSwitchVersion+inputParameterValue, longSwitchVersion, paramSwitch });

        testCases.add(new Object[] { expectedParameterValue, longSwitchVersion+"="+inputParameterValue, longSwitchVersion, paramSwitch });
        testCases.add(new Object[] { expectedParameterValue, longSwitchVersion+"|"+inputParameterValue, longSwitchVersion, paramSwitch });

        return testCases;
    }

}
