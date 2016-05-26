package com.dslplatform.mojo;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Main;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

@Mojo(name = DslPlatformMojo.GOAL)
public class DslPlatformMojo extends AbstractMojo {

	public static final String GOAL = "execute";

	private final MojoContext context = new MojoContext(getLog());

	@Component
	private MavenProject project;

	@Parameter(required = true)
	private String properties;

	@Parameter(property = "plugins", defaultValue = ".")
	private String plugins;

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String value) {
		this.properties = value;
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

		if (this.properties == null || this.properties.length() == 0) {
			throw new MojoExecutionException("The properties file not specified");
		}
		String path = Utils.resourceAbsolutePath(this.properties);
		if (path == null) {
			throw new MojoExecutionException("Specified properties file not found: " + properties);
		}
		this.context.put("properties", path);

		if (plugins == null || plugins.length() == 0) {
			plugins = ".";
		}
		File pluginsFile = new File(plugins);
		if (!pluginsFile.exists()) {
			throw new MojoExecutionException("Specified plugins path not found: " + pluginsFile.getAbsolutePath());
		} else if (!pluginsFile.isDirectory()) {
			throw new MojoExecutionException("Please specify path to directory, not a specific file: " + pluginsFile.getAbsolutePath());
		}

		List<CompileParameter> params = Main.initializeParameters(context, pluginsFile.getAbsolutePath());

		if (!Main.processContext(context, params)) {
			throw new MojoExecutionException(context.errorLog.toString());
		}

		context.close();
	}
}
