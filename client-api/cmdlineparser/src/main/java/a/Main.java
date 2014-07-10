package a;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.compiler.client.api.config.PropertyLoader;
import com.dslplatform.compiler.client.api.config.StreamLoader;
import com.dslplatform.compiler.client.cmdline.parser.Arguments;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsReader;
import com.dslplatform.compiler.client.cmdline.parser.ArgumentsValidator;
import com.dslplatform.compiler.client.cmdline.parser.CachingArgumentsProxy;
import com.dslplatform.compiler.client.io.PathExpander;

public class Main {
    private static void main(final String[] args) throws IOException {

        // TODO: generate java android
        // generate-migration-sql ...

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");


        final String[] argses = (
                "-i|kekec|-ibekec|-i|tukac|-ipero|--project-id=pero|--project-id|pero2|--project-id=x|--project-id=|y|-iqwe|--project-id=1-2-3-4-5|"
                + "-uu|"
                + "--active-record|--active-record=yes|"
                + "-f~/code/ngs/dsl-compiler-client/client-api/cmdlineparser/src/test/resources/example.props|"
                + "-tjava client,java,scala-server|"
                + "--project-name|KiklopZdenski|"
                + "--package-name=com.`dsl platform`.test|--target=!java,c# server"
                ).split("\\|");

        final Queue<String> q = new ArrayDeque<String>();
        for (final String a : argses) q.add(a);

        final Logger logger = LoggerFactory.getLogger("keekc");

        final PropertyLoader propertyLoader = new PropertyLoader(logger,
                new StreamLoader(logger, new PathExpander(logger)));

        final Arguments a = new CachingArgumentsProxy(
                logger,
                new ArgumentsValidator(logger,
                        new ArgumentsReader(logger, propertyLoader).readArguments(q)));

        System.out.println(a.getActions().getActionSet().toString());
//        System.out.println(a.getCachePath());
        System.out.println(a.getDSLPath());
        System.out.println(a.getLoggingLevel());
        System.out.println(a.getOutputPath());
        System.out.println(a.getPackageName());
        System.out.println(a.getPassword());
        System.out.println(a.getProjectID());
        System.out.println(a.getProjectName());
        System.out.println(a.getProjectPropertiesPath());
        System.out.println(a.getTargets());
        System.out.println(a.getUsername());
        System.out.println(a.getPassword());
    }
}
