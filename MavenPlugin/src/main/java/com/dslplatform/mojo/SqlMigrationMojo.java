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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mojo(name = SqlMigrationMojo.GOAL)
public class SqlMigrationMojo extends AbstractMojo {

	public static final String GOAL = "sql-migration";

	@Parameter(property = "compiler")
	private String compiler;

	@Parameter(property = "dsl", defaultValue = "dsl")
	private String dsl;

	@Parameter(property = "sql", defaultValue = "sql")
	private String sql;

	@Parameter(property = "postgres")
	private String postgres;

	@Parameter(property = "oracle")
	private String oracle;

	@Parameter(property = "applySql", defaultValue = "true")
	private boolean applySql;

	private Map<CompileParameter, String> compileParametersParsed = new HashMap<CompileParameter, String>();
	private Map<Settings.Option, String> flagsParsed = new HashMap<Settings.Option, String>();

	public void setCompiler(String value) {
		if (value == null) return;
		this.compiler = value;
		compileParametersParsed.put(DslCompiler.INSTANCE, value);
	}

	public String getCompiler() {
		return this.compiler;
	}

	public void setDsl(String value) {
		if (value == null) return;
		this.dsl = value;
		compileParametersParsed.put(DslPath.INSTANCE, value);
	}

	public String getDsl() {
		return this.dsl;
	}

	public void setSql(String value) {
		if (value == null) return;
		this.sql = value;
		compileParametersParsed.put(SqlPath.INSTANCE, value);
	}

	public String getSql() {
		return this.sql;
	}

	public void setPostgres(String value) {
		if (value == null) return;
		this.postgres = value;
		compileParametersParsed.put(PostgresConnection.INSTANCE, value);
	}

	public String getPostgres() {
		return this.postgres;
	}

	public void setOracle(String value) {
		if (value == null) return;
		this.oracle = value;
		compileParametersParsed.put(OracleConnection.INSTANCE, value);
	}

	public String getOracle() {
		return this.oracle;
	}

	public void setApplySql(boolean applySql) {
		this.applySql = applySql;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		Utils.cleanupParameters(this.compileParametersParsed);
		// TODO: Default values
		Utils.sanitizeDirectories(this.compileParametersParsed);

		if (oracle == null && postgres == null) {
			throw new MojoExecutionException("Neither Oracle or Postgres jdbc url specified. Please specify one, for example: <postgres>localhost/database?user=postgres</postgres>");
		}

		MojoContext context = new MojoContext(getLog())
				.with(this.flagsParsed)
				.with(this.compileParametersParsed)
				.with(Migration.INSTANCE)
				.with(Force.INSTANCE)
				.with(Download.INSTANCE)
				.with(Prompt.INSTANCE);

		if (this.applySql) context.with(ApplyMigration.INSTANCE);

		List<CompileParameter> params = Main.initializeParameters(context, ".");

		if (!Main.processContext(context, params)) {
			throw new MojoExecutionException(context.errorLog.toString());
		}

		context.close();
	}
}
