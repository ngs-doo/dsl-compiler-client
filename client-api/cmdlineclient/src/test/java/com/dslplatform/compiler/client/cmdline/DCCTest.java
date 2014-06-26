package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.Api;
import com.dslplatform.compiler.client.ApiImpl;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.tools.MockCommandLinePrompt;
import com.dslplatform.compiler.client.cmdline.tools.TestingOutput;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

abstract public class DCCTest {
    protected Logger logger = LoggerFactory.getLogger("dcc-test");

    protected Arguments arguments = makeArguments();
    protected TestingOutput output = new TestingOutput();
    protected Api api = makeApi();
    protected CommandLinePrompt clcp = makeClcp();
    protected CLCAction action = makeAction();

    protected abstract Arguments makeArguments();

    protected CommandLinePrompt makeClcp() {
        return  new MockCommandLinePrompt(true, true, true, true, true, true, true, true, true, true);
    }

    public CLCAction makeAction() {
        return new ActionDefinition(api, logger, output, arguments, clcp);
    }

    public Api makeApi() {
        try {
            return new ApiImpl();
        } catch (IOException e) {
            return new ApiImpl(null, null, null);
        }
    }

    @Before
    public void setUp() throws IOException {
        Main.process(action);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        /** Clean up.*/
    }
}
