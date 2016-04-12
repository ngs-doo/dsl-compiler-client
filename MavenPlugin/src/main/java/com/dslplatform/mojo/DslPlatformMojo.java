package com.dslplatform.mojo;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.*;
import com.dslplatform.mojo.context.MojoContext;
import com.dslplatform.mojo.utils.Utils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Mojo(name = DslPlatformMojo.GOAL)
public class DslPlatformMojo
		extends AbstractMojo {

	public static final String GOAL = "execute";

	@Parameter(defaultValue = "src/generated/java")
	private String generatedSourcesTarget;

	@Parameter(defaultValue = "src/main/resources/META-INF/services")
	private String servicesManifestTarget;

	@Parameter(property = "targets")
	private Map<String, String> targets_;

	@Parameter(property = "flags")
	private String[] flags_;

	@Parameter(property = "compileParameters")
	private Map<String, String> compileParameters_;

	private Map<Targets.Option, String> targetsParsed;

	public void setTargets(Map<String, String> targets) {
		getLog().info("Setting targets");
		this.targets_ = targets;
		this.targetsParsed = new HashMap<Targets.Option, String>();
		for (Map.Entry<String, String> kv : targets.entrySet()) {
			String key = kv.getKey();
			String value = kv.getValue();

			Targets.Option option = Utils.targetOptionFrom(key);
			if (option != null) this.targetsParsed.put(option, value);
		}
	}

	private List<Settings.Option> flagsParsed;

	public void setFlags(String[] flags) {
		getLog().info("Setting flags");
		this.flags_ = flags;
		this.flagsParsed = new ArrayList<Settings.Option>(flags.length);
		for (String setting : flags) {

			Settings.Option option = Utils.settingsOptionFrom(setting);
			if (option != null) this.flagsParsed.add(option);
		}
	}

	private Map<CompileParameter, String> compileParametersParsed;

	public void setCompileParameters(Map<String, String> compileParameters) {
		getLog().info("Setting compile parameters");
		this.compileParameters_ = compileParameters;
		this.compileParametersParsed = new HashMap<CompileParameter, String>();
		for (Map.Entry<String, String> kv : compileParameters.entrySet()) {
			String key = kv.getKey();
			String value = kv.getValue();

			CompileParameter compileParameter = Utils.compileParameterFrom(key);
			if (compileParameter != null) this.compileParametersParsed.put(compileParameter, value);
		}
	}

	public void execute()
			throws MojoExecutionException, MojoFailureException {
		Utils.cleanupParameters(compileParametersParsed);
		// TODO: Default values
		Utils.sanitizeDirectories(compileParametersParsed);

		MojoContext context = new MojoContext(getLog())
				.with(targetsParsed)
				.with(compileParametersParsed)
				.with(flagsParsed)
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
		}

		context.close();
	}

	private void copyGeneratedSources(MojoContext context) throws MojoExecutionException{
		File tmpPath = TempPath.getTempProjectPath(context);
		getLog().info("Temp path: " + tmpPath.getAbsolutePath());
		for (Targets.Option target : this.targetsParsed.keySet()) {
			// TODO: Multiple java targets will overwrite each other
			File generatedSources = new File(tmpPath.getAbsolutePath(), target.name());
			Utils.createDirIfNotExists(this.generatedSourcesTarget);
			Utils.copyFolder(generatedSources, new File(this.generatedSourcesTarget), context);
		}
	}

	protected void registerServices(MojoContext context) throws MojoExecutionException {
		// TODO: Add check if we generated code
		String namespace = context.get(Namespace.INSTANCE);
		String service = namespace == null ? "Boot" : namespace + ".Boot";
		Utils.createDirIfNotExists(this.servicesManifestTarget);
		File servicesRegistration = new File(servicesManifestTarget, "org.revenj.extensibility.SystemAspect");
		Utils.writeToFile(context, servicesRegistration, service);
	}

	protected <K, V> void write(Map<K, V> map) {
		for (Map.Entry kv : map.entrySet()) {
			getLog().info(kv.getKey().toString() + " : " + kv.getValue());
		}
	}

	private <K> void write(List<K> list) {
		getLog().info(list.toString());
	}

}
