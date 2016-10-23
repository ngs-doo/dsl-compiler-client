package com.dslplatform.mojo;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = GenerateCodeMojo.GOAL)
public class GenerateCodeMojo extends AbstractMojo {

	public static final String GOAL = "generate-code";

	private static final String SERVICES_FILE = "org.revenj.extensibility.SystemAspect";

	private final MojoContext context = new MojoContext(getLog());

	@Component
	private MavenProject project;

	@Parameter(property = "compiler")
	private String compiler;

	@Parameter(property = "generatedSources", defaultValue = "target/generated-sources")
	private String generatedSources;

	@Parameter(property = "servicesManifest", defaultValue = "target/classes/META-INF/services")
	private String servicesManifest;

	@Parameter(property = "target", required = true)
	private String target;

	@Parameter(property = "dsl", defaultValue = "dsl")
	private String dsl;

	@Parameter(property = "namespace", defaultValue = "")
	private String namespace;

	@Parameter(property = "options")
	private String[] options;

	@Parameter(property = "plugins", defaultValue = ".")
	private String plugins;

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setCompiler(String value) {
		this.compiler = value;
	}

	public String getCompiler() {
		return this.compiler;
	}

	public void setGeneratedSources(String value) {
		this.generatedSources = value;
	}

	public String getGeneratedSources() {
		return generatedSources;
	}

	public void setServicesManifest(String value) {
		this.servicesManifest = value;
	}

	public String getServicesManifest() {
		return servicesManifest;
	}

	public void setTarget(String value) {
		this.target = value;
	}

	public String getTarget() {
		return target;
	}

	public void setDsl(String value) {
		this.dsl = value;
	}

	public String getDsl() {
		return dsl;
	}

	public void setNamespace(String value) {
		this.namespace = value;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setOptions(String[] value) {
		this.options = value;
	}

	public String[] getOptions() {
		return options;
	}

	public void setPlugins(String value) {
		this.plugins = value;
	}

	public String getPlugins() {
		return plugins;
	}

	public MojoContext getContext() {
		return context;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (target == null || target.length() == 0) {
			throw new MojoExecutionException("Target not specified. Please specify target, for example: <target>revenj.java</target>");
		}
		Targets.Option parsedTarget = Utils.targetOptionFrom(target);
		if (parsedTarget == null) {
			throw new MojoExecutionException("Invalid target specified: " + target);
		}
		context.put(Targets.INSTANCE, parsedTarget.toString());

		if (namespace != null && namespace.length() > 0) {
			context.put(Namespace.INSTANCE, namespace);
		}

		String formattedSettings = Utils.parseSettings(this.options, context.log);
		if (formattedSettings != null && formattedSettings.length() > 0) {
			context.put(Settings.INSTANCE, formattedSettings);
		}

		this.context.with(Settings.Option.SOURCE_ONLY);

		Utils.runCompiler(context, plugins, dsl, compiler);

		copyGeneratedSources(context, parsedTarget);
		registerServices(context);
		// This supposedly adds generated sources to maven compile classpath:
		project.addCompileSourceRoot(this.generatedSources);

		context.close();
	}

	private void registerServices(MojoContext context) throws MojoExecutionException {
		String namespace = context.get(Namespace.INSTANCE);
		String service = namespace == null || namespace.length() == 0 ? "Boot" : namespace + ".Boot";
		File boot = new File(generatedSources, service.replace(".", File.pathSeparator) + ".java");
		if (boot.exists()) {
			Utils.createDirIfNotExists(this.servicesManifest);
			File servicesRegistration = new File(servicesManifest, SERVICES_FILE);
			context.show("Boot file exists. Creating META-INF resources in:" + servicesRegistration.getAbsolutePath());
			Either<String> content = com.dslplatform.compiler.client.Utils.readFile(servicesRegistration);
			boolean empty = !content.isSuccess() || content.get().isEmpty();
			if (empty) {
				context.log("File empty. Appending...");
				Utils.appendToFile(context, servicesRegistration, service);
			} else if (!(service.equals(content.get())
					|| content.get().startsWith(service + "\n") || content.get().startsWith(service + "\r")
					|| content.get().contains("\n" + service + "\n") || content.get().contains("\n" + service + "\r")
					|| content.get().endsWith("\n" + service))) {
				context.log("File not empty but missing service. Appending...");
				Utils.appendToFile(context, servicesRegistration, "\n" + service);
			} else {
				context.log("File already contains service.");
			}
		}
	}

	private void copyGeneratedSources(MojoContext context, Targets.Option parsedTarget) throws MojoExecutionException {
		File tmpPath = TempPath.getTempProjectPath(context);
		File generatedSources = new File(tmpPath.getAbsolutePath(), parsedTarget.name());
		context.show("Copying generated files from " + generatedSources.getAbsolutePath() + " to " + this.generatedSources);
		Utils.createDirIfNotExists(this.generatedSources);
		Utils.copyFolder(generatedSources, new File(this.generatedSources), context);
	}
}
