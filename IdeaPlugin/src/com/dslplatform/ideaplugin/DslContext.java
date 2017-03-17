package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.*;
import com.intellij.openapi.diagnostic.Logger;

class DslContext extends Context {

	private final Logger logger;

	DslContext(Logger logger) {
		this.logger = logger;
	}

	public void show(String... values) {
		for (String v : values) {
			logger.info(v);
		}
	}

	public void log(String value) {
		if (logger.isDebugEnabled()) {
			logger.debug(value);
		}
	}

	public void log(char[] value, int len) {
		if (logger.isDebugEnabled()) {
			logger.debug(new String(value, 0, len));
		}
	}

	public void warning(String value) {
		logger.warn(value);
	}

	public void warning(Exception ex) {
		logger.warn(ex.getMessage());
		if (logger.isDebugEnabled()) {
			logger.debug(ex.toString());
		}
	}

	public void error(String value) {
		logger.warn(value);
	}

	public void error(Exception ex) {
		logger.warn(ex.getMessage());
		if (logger.isDebugEnabled()) {
			logger.debug(ex.toString());
		}
	}
}