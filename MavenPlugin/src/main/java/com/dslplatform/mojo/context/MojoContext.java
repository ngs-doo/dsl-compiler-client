package com.dslplatform.mojo.context;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.parameters.Settings;
import com.dslplatform.compiler.client.parameters.Targets;
import org.apache.maven.plugin.logging.Log;

import java.util.List;
import java.util.Map;

public class MojoContext extends Context {
	public final StringBuilder errorLog = new StringBuilder();

	private final Log log;

	public MojoContext(Log log) {
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
		if (settings != null) {
			for (Settings.Option option : settings)
				this.with(option);
		}
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
		if (log.isInfoEnabled()) {
			for (String v : values) {
				log.info(v);
			}
		}
	}

	public void log(String value) {
		if (log.isDebugEnabled()) {
			log.debug(value);
		}
	}

	public void log(char[] value, int len) {
		if (log.isDebugEnabled()) {
			log.debug(new String(value, 0, len));
		}
	}

	public void warning(String value) {
		errorLog.append(value + "\n");
		if (log.isWarnEnabled()) {
			log.warn(value);
		}
	}

	public void warning(Exception ex) {
		errorLog.append(ex.getMessage());
		if (log.isWarnEnabled()) {
			log.warn(ex);
		}
	}

	public void error(String value) {
		errorLog.append(value + "\n");
		if (log.isErrorEnabled()) {
			log.error(value);
		}
	}

	public void error(Exception ex) {
		errorLog.append(ex.getMessage());
		if (log.isErrorEnabled()) {
			log.error(ex);
		}
	}
}
