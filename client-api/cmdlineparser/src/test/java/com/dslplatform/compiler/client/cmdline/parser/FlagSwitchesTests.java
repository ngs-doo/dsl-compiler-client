package com.dslplatform.compiler.client.cmdline.parser;
import static org.junit.Assert.assertEquals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.io.PathExpander;

@RunWith(Parameterized.class)
public class FlagSwitchesTests {

    final static Logger logger = LoggerFactory.getLogger(FlagSwitchesTests.class);

    private final String inputValue;
    private final String expectedParsedValue;
    private final String switchTestName;
    private final ParamSwitches theActualTestParamSwitch;

    public FlagSwitchesTests(
            final String expectedParsedValue,
            final String inputValue,
            final String switchTestName,
            final ParamSwitches theActualTestParamSwitch) {
        this.expectedParsedValue = expectedParsedValue;
        this.inputValue = inputValue;
        this.switchTestName = switchTestName;
        this.theActualTestParamSwitch = theActualTestParamSwitch;
    }

    @Test
    public void testForFlag() throws Exception {
        try {
            /* The switch we are testing, the expected parsed value,
            * input value, and the switch we are testing on
            * are given to us by theTestProvider() */

            final String inputValue = this.inputValue;
            final String expectedParsedValue = this.expectedParsedValue;

            logger.info("========");
            logger.info("The flag we are testing: " + theActualTestParamSwitch);
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
            logger.info("Asserting for string pair: (" + expectedParsedValue + ", " + actualPattern + ")");

            assertEquals(expectedParsedValue, actualPattern);
        } catch (final Exception e) {
            logger.info("Exception: " + e.getMessage());
            throw (e);
        }

    }

    @Parameterized.Parameters(name = "Testing for switch:  {2}  on single pattern: {0}")
    public static Iterable<Object[]> theTestProvider() {
        final List<Object[]> inputPatterns = new ArrayList<Object[]>();

        final Set<ParamSwitches> booleanFlagSwitches =
                EnumSet.of(
                          ParamSwitches.WITH_ACTIVE_RECORD_SWITCHES
                        , ParamSwitches.WITH_HELPER_METHODS_SWITCHES
                        , ParamSwitches.WITH_JACKSON_SWITCHES
                        , ParamSwitches.WITH_JAVA_BEANS_SWITCHES
                        , ParamSwitches.SKIP_DIFF_SWITCHES
                        , ParamSwitches.ALLOW_UNSAFE_SWITCHES);

        logger.info(ParamSwitches.WITH_ACTIVE_RECORD_SWITCHES.toString());

        for (final ParamSwitches paramSwitch : booleanFlagSwitches) {
            logger.info("Doing paramSwitch: " + paramSwitch);
            inputPatterns.addAll(inputPatternsForParamSwitch(paramSwitch));
        }

        return inputPatterns;
    }

    private static List<Object[]> inputPatternsForParamSwitch(final ParamSwitches paramSwitch) {
        final List<Object[]> inputPatterns = new ArrayList<Object[]>();

        final List<String> switches = Arrays.asList(paramSwitch.getSwitches());

        final String switchString = switches.get(0);

        /* It's a flag */
        inputPatterns.add(new Object[] { Boolean.toString(true), switchString, switchString, paramSwitch });

        return inputPatterns;
    }

    /**
     * Returns the expected output value of the argument
     */
    private String getExpectedOutputString(final Arguments args) {
        switch (theActualTestParamSwitch) {
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
}
