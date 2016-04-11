package com.dslplatform.mojo.context;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.parameters.Settings;
import com.dslplatform.compiler.client.parameters.Targets;
import org.apache.maven.plugin.logging.Log;

import java.util.List;
import java.util.Map;

public class MojoContext extends Context {
	public final StringBuilder showLog = new StringBuilder();
	public final StringBuilder errorLog = new StringBuilder();
	public final StringBuilder traceLog = new StringBuilder();

	private final Log log;

	public MojoContext(Log log) {
		super();
		this.log = log;
	}

	public MojoContext with(Targets.Option option) {
		this.put(option.toString(), null);
		return this;
	}

	public MojoContext with(Targets.Option option, String value) {
		this.put(option.toString(), value);
		return this;
	}

	public MojoContext with(Settings.Option option) {
		this.put(option.toString(), null);
		return this;
	}

	public MojoContext with(Settings.Option option, String value) {
		this.put(option.toString(), value);
		return this;
	}

	public MojoContext with(CompileParameter parameter) {
		this.put(parameter, null);
		return this;
	}

	public MojoContext with(CompileParameter parameter, String value) {
		this.put(parameter, value);
		return this;
	}

	public MojoContext with(String option) {
		this.put(option, null);
		return this;
	}

	public MojoContext with(String option, String value) {
		this.put(option, value);
		return this;
	}

	public MojoContext with(List<Settings.Option> settings) {
		if (settings != null)
			for (Settings.Option option : settings)
				this.with(option);
		return this;
	}

	public <K> MojoContext with(Map<K, String> compileParameters) {
		if (compileParameters != null)
			for (Map.Entry<K, String> kv : compileParameters.entrySet()) {
				K parameter = kv.getKey();
				String value = kv.getValue();
				if (parameter instanceof CompileParameter)
					this.put((CompileParameter) parameter, value);
				if (parameter instanceof Targets.Option)
					this.put(parameter.toString(), value);
			}
		return this;
	}

	public void show(String... values) {
		for (String v : values) {
			showLog.append(v + "\n");
			log.info(v);
		}
	}

	public void log(String value) {
		traceLog.append(value + "\n");
		log.info(value);
	}

	public void log(char[] value, int len) {
		traceLog.append(value, 0, len);
	}

	public void error(String value) {
		errorLog.append(value + "\n");
		log.error(value);
	}

	public void error(Exception ex) {
		errorLog.append(ex.getMessage());
		traceLog.append(ex.toString());
		log.error(ex);
	}

}
