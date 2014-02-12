package com.dslplatform.compiler.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dslplatform.compiler.client.api.ApiProperties;
import com.dslplatform.compiler.client.api.commons.PathExpander;
import com.dslplatform.compiler.client.cmdline.logger.LoggerSystem;
import com.dslplatform.compiler.client.cmdline.login.LoginConsole;
import com.dslplatform.compiler.client.cmdline.output.OutputConsole;
import com.dslplatform.compiler.client.cmdline.output.OutputSystem;
import com.dslplatform.compiler.client.cmdline.prompt.PromptConsole;
import com.dslplatform.compiler.client.cmdline.prompt.PromptSystem;
//import com.dslplatform.compiler.client.gui.LoginSwing;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Logger.Level;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

// format: OFF

public class Main {
    private static Logger getLogger() {
        return new LoggerSystem(Level.ERROR);
    }

    // --------------------------------------------------------------------

    private static Output getOutput(
            final Logger logger) {
        for (final Output output : new Output[] {
//                    new OutputJAnsi(),
                    new OutputConsole(),
                    new OutputSystem()
                }) {

            logger.trace("Testing output availability: " + output);

            if (output.isAvailable()) {
                return output;
            }
        }

        throw new RuntimeException(
                "Should not happen: Could not initialize output!");
    }

    // --------------------------------------------------------------------

    private static Prompt getPrompt(
            final Logger logger,
            final Output output) {
        for (final Prompt prompt : new Prompt[] {
//                    new PromptJLine(output),
                    new PromptConsole(output),
                    new PromptSystem(output)
                }) {

            logger.trace("Testing prompt availability: " + prompt);

            if (prompt.isAvailable()) {
                return prompt;
            }
        }

        throw new RuntimeException(
                "Should not happen: Could not initialize prompt!");
    }

    // --------------------------------------------------------------------

    private static Login getLogin(
            final Logger logger,
            final Output output,
            final Prompt prompt,
            final ApiProperties apiProperties) {
        for (final Login login : new Login[] {
//                    new LoginSwing(logger, output, apiProperties),
                    new LoginConsole(logger, prompt)
                }) {

            logger.trace("Testing login availability: " + login);

            if (login.isAvailable()) {
                return login;
            }
        }

        throw new RuntimeException(
                "Should not happen: Could not initialize output!");
    }

    // --------------------------------------------------------------------

    private static ApiProperties readFromStream(
            final Logger logger,
            final InputStream is) throws IOException {
        final Properties properties = new Properties();
        properties.load(is);
        return new ApiProperties(logger, properties);
    }

    private static ApiProperties readApiProperties(
            final Logger logger) throws IOException {
        final String override = System.getProperty("dsl-clc-api-properties");
        if (override == null) {
            logger.trace("Reading default api.properties ...");
            return readFromStream(
                    logger,
                    Main.class.getResourceAsStream("api/api.properties"));
        }

        logger.info("Overriding api.properties with: " + override);
        try {
            return readFromStream(
                    logger,
                    Main.class.getResourceAsStream(override));
        }
        catch (final Exception e) {
            final PathExpander pathExpander = new PathExpander(logger);
            final FileInputStream fis = new FileInputStream(
                    pathExpander.expandPath(override));
            try {
                return readFromStream(logger, fis);
            }
            finally {
                fis.close();
            }
        }
    }

    // --------------------------------------------------------------------

    public static void main(
            final String[] args) {
        try {
            final Logger logger = getLogger();
            logger.debug("Selected logger: " + logger);

            final Output output = getOutput(logger);
            logger.debug("Selected output: " + output);

            final Prompt prompt = getPrompt(logger, output);
            logger.debug("Selected prompt: " + prompt);

            final ApiProperties apiProperties = readApiProperties(logger);

            final Login login = getLogin(logger, output, prompt, apiProperties);
            logger.debug("Selected login: " + login);

            new com.dslplatform.compiler.client.cmdline.Main(logger, prompt,
                    output, login, apiProperties).process(args);

            System.exit(0);
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
