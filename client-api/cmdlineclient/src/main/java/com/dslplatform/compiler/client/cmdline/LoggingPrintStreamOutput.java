package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.io.PrintStreamOutput;
import org.slf4j.Logger;

public class LoggingPrintStreamOutput extends PrintStreamOutput {

    Logger logger;

    public LoggingPrintStreamOutput(Logger logger) {
        super();
        this.logger = logger;
    }

    @Override
    public void print(final String message) {
        logger.info(message);
        super.print(message);
    }

    @Override
    public void println(final String message) {
        logger.info(message);
        super.println(message);
    }
}
