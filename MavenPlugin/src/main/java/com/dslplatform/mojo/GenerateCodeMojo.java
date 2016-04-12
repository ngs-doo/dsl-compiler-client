package com.dslplatform.mojo;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.*;
import com.dslplatform.mojo.context.MojoContext;
import com.dslplatform.mojo.utils.Utils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mojo(name = GenerateCodeMojo.GOAL)
public class GenerateCodeMojo extends AbstractMojo {

	public static final String GOAL = "generate-code";

	private static final String SERVICES_FILE = "org.revenj.extensibility.SystemAspect";

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(property = "generatedSourcesTarget", defaultValue = "target/generated-sources")
	private String generatedSourcesTarget;

	@Parameter(property = "servicesManifestTarget", defaultValue = "target/classes/META-INF/services")
	private String servicesManifestTarget;

	@Parameter(property = "target", required = true)
	private String target;

	@Parameter(property = "dsl", defaultValue = "dsl")
	private String dsl;

	@Parameter(property = "namespace", defaultValue = "")
	private String namespace;

	@Parameter(property = "settings")
	private String[] settings;

	private Targets.Option targetParsed;
	private Map<CompileParameter, String> compileParametersParsed = new HashMap<CompileParameter, String>();
	private List<Settings.Option> settingsParsed = new ArrayList<Settings.Option>();

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setGeneratedSourcesTarget(String value) {
		this.generatedSourcesTarget = value;
	}

	public String getGeneratedSourcesTarget() {
		return generatedSourcesTarget;
	}

	public void setServicesManifestTarget(String value) {
		this.servicesManifestTarget = value;
	}

	public String getServicesManifestTarget() {
		return servicesManifestTarget;
	}

	public void setTarget(String value) {
		getLog().info("Setting target " + value);
		if (value == null) return;
		this.target = value;
		this.targetParsed = Utils.targetOptionFrom(value);
		getLog().info("Parsed value " + targetParsed);
	}

	public String getTarget() {
		return target;
	}

	public void setDsl(String value) {
		if (value == null) return;
		this.dsl = value;
		compileParametersParsed.put(DslPath.INSTANCE, value);
	}

	public String getDsl() {
		return dsl;
	}

	public void setNamespace(String value) {
		this.namespace = value;
		compileParametersParsed.put(Namespace.INSTANCE, value);
	}

	public String getNamespace() {
		return namespace;
	}

	public void setSettings(String[] value) {
		getLog().info("Setting settings");
		this.settings = value;
		this.settingsParsed = new ArrayList<Settings.Option>(settings.length);
		for (String setting : settings) {
			Settings.Option option = Utils.settingsOptionFrom(setting);
			if (option != null) {
				this.settingsParsed.add(option);
			}
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		Utils.cleanupParameters(this.compileParametersParsed);
		// TODO: Default values
		Utils.sanitizeDirectories(this.compileParametersParsed);

		if (targetParsed == null) {
			throw new MojoExecutionException("Target not specified. Please specify target, for example: <target>revenj.java</target>");
		}

		MojoContext context = new MojoContext(getLog())
				.with(this.targetParsed)
				.with(this.settingsParsed)
				.with(this.compileParametersParsed)
				.with(Force.INSTANCE)
				.with(Download.INSTANCE)
				.with(Prompt.INSTANCE)
				.with(Settings.Option.SOURCE_ONLY);

		List<CompileParameter> params = Main.initializeParameters(context, ".");

		if (!Main.processContext(context, params)) {
			throw new MojoExecutionException(context.errorLog.toString());
		} else {
			// Copy generated sources
			copyGeneratedSources(context);
			registerServices(context);
			// This supposedly adds generated sources to maven compile classpath:
			project.addCompileSourceRoot(this.generatedSourcesTarget);
		}

		context.close();
	}

	protected void registerServices(MojoContext context) throws MojoExecutionException {
		String namespace = context.get(Namespace.INSTANCE);
		String service = namespace == null ? "Boot" : namespace + ".Boot";
		Utils.createDirIfNotExists(this.servicesManifestTarget);
		File servicesRegistration = new File(servicesManifestTarget, SERVICES_FILE);
		Utils.writeToFile(context, servicesRegistration, service);
	}

	private void copyGeneratedSources(MojoContext context) throws MojoExecutionException {
		File tmpPath = TempPath.getTempProjectPath(context);
		File generatedSources = new File(tmpPath.getAbsolutePath(), targetParsed.name());
		Utils.createDirIfNotExists(this.generatedSourcesTarget);
		Utils.copyFolder(generatedSources, new File(this.generatedSourcesTarget), context);
	}


}
