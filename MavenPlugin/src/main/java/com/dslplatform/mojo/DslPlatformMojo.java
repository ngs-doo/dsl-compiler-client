package com.dslplatform.mojo;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.*;
import com.dslplatform.mojo.context.MojoContext;
import com.dslplatform.mojo.utils.Utils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Mojo(name = DslPlatformMojo.GOAL)
public class DslPlatformMojo
		extends AbstractMojo {

	public static final String GOAL = "execute";

	private final MojoContext context = new MojoContext(getLog());

	@Component
	private MavenProject project;

	@Parameter(required = true)
	private Properties properties;

	private String propertiesAbsolutePath;

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(String path) {
		propertiesAbsolutePath = Utils.resourceAbsolutePath(path);
		getLog().info("Setting properties from file: " + propertiesAbsolutePath);
		try {
			if (propertiesAbsolutePath != null) {
				properties = new Properties();
				properties.load(new FileInputStream(propertiesAbsolutePath));

			}
		} catch (IOException e) {
			this.properties = null;
		}
	}

	public String getPropertiesAbsolutePath() {
		return propertiesAbsolutePath;
	}

	public void setPropertiesAbsolutePath(String propertiesAbsolutePath) {
		this.propertiesAbsolutePath = propertiesAbsolutePath;
	}

	public MojoContext getContext() {
		return context;
	}

	public void execute()
			throws MojoExecutionException, MojoFailureException {

		if(this.properties == null) {
			throw new MojoExecutionException("The given properties file not found: " + propertiesAbsolutePath);
		}

		this.context
				.with(new PropertiesFile(new ArrayList<CompileParameter>()), propertiesAbsolutePath);
				;

		List<CompileParameter> params = Main.initializeParameters(context, ".");

		if (!Main.processContext(context, params)) {
			throw new MojoExecutionException(context.errorLog.toString());
		}

		context.close();
	}

}
