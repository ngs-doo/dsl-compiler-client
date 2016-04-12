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

	public void setDsl(String path) {
		if (path == null) return;
		this.dsl = path;
		compileParametersParsed.put(DslPath.INSTANCE, this.dsl);
	}

	public String getDsl() {
		return this.dsl;
	}

	public void setSql(String path) {
		if (path == null) return;
		this.sql = path;
		compileParametersParsed.put(SqlPath.INSTANCE, this.sql);
	}

	public String getSql() {
		return this.sql;
	}

	public void setPostgres(String connectionString) {
		if (connectionString == null) return;
		this.postgres = connectionString;
		compileParametersParsed.put(PostgresConnection.INSTANCE, this.postgres);
	}

	public String getPostgres() {
		return this.postgres;
	}

	public void setOracle(String connectionString) {
		if (connectionString == null) return;
		this.oracle = connectionString;
		compileParametersParsed.put(OracleConnection.INSTANCE, this.oracle);
	}

	public String getOracle() {
		return this.oracle;
	}


	public void execute() throws MojoExecutionException, MojoFailureException {
		Utils.cleanupParameters(this.compileParametersParsed);
		// TODO: Default values
		Utils.sanitizeDirectories(this.compileParametersParsed);

		if (oracle == null && postgres == null) {
			throw new MojoExecutionException("Neither Oracle or Postgres jdbc url not specify. Please specify one, for example: <postgres>localhost/database?user=postgres</postgres>");
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
