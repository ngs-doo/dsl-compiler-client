package com.dslplatform.mojo;

import com.dslplatform.compiler.client.parameters.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo(name = SqlMigrationMojo.GOAL)
public class SqlMigrationMojo extends AbstractMojo {

	public static final String GOAL = "sql-migration";

	private final MojoContext context = new MojoContext(getLog());

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

	@Parameter(property = "applySql", defaultValue = "false")
	private boolean applySql;

	@Parameter(property = "plugins", defaultValue = ".")
	private String plugins;

	public void setCompiler(String value) {
		this.compiler = value;
	}

	public String getCompiler() {
		return this.compiler;
	}

	public void setDsl(String value) {
		this.dsl = value;
	}

	public String getDsl() {
		return this.dsl;
	}

	public void setSql(String value) {
		this.sql = value;
	}

	public String getSql() {
		return this.sql;
	}

	public void setPostgres(String value) {
		this.postgres = value;
	}

	public String getPostgres() {
		return this.postgres;
	}

	public void setOracle(String value) {
		this.oracle = value;
	}

	public String getOracle() {
		return this.oracle;
	}

	public void setApplySql(boolean applySql) {
		this.applySql = applySql;
	}

	public boolean getApplySql() {
		return applySql;
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
		if (sql != null && sql.length() > 0) {
			String sqlPath = Utils.createDirIfNotExists(sql);
			this.context.put(SqlPath.INSTANCE, sqlPath);
		}
		if (oracle == null && postgres == null) {
			throw new MojoExecutionException("Neither Oracle or Postgres jdbc url specified. Please specify one, for example: <postgres>localhost/database?user=postgres</postgres>");
		}
		if (postgres != null) {
			this.context.put(PostgresConnection.INSTANCE, postgres);
		}
		if (oracle != null) {
			this.context.put(OracleConnection.INSTANCE, oracle);
		}
		this.context.with(Migration.INSTANCE);

		if (this.applySql) context.with(ApplyMigration.INSTANCE);

		Utils.runCompiler(context, plugins, dsl, compiler);

		context.close();
	}
}
