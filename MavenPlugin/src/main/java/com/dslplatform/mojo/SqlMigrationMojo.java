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

	@Parameter(name = "dsl", property = "dsl", defaultValue = "dsl")
	private String dslPath;

	@Parameter(name = "sql", property = "sql", defaultValue = "sql")
	private String sqlPath;

	@Parameter(name = "postgres", property = "postgres")
	private String postgresConnectionString;

	@Parameter(name = "oracle", property = "oracle")
	private String oracleConnectionString;

	@Parameter(name = "applySql", property = "applySql", defaultValue = "true")
	private boolean applySql;

	private Map<CompileParameter, String> compileParametersParsed = new HashMap<CompileParameter, String>();
	private Map<Settings.Option, String> flagsParsed = new HashMap<Settings.Option, String>();

	public void setDsl(String path) {
		if (path == null) return;
		this.dslPath = path;
		compileParametersParsed.put(DslPath.INSTANCE, this.dslPath);
	}

	public String getDsl() {
		return this.dslPath;
	}

	public void setSql(String path) {
		if (path == null) return;
		this.sqlPath = path;
		compileParametersParsed.put(SqlPath.INSTANCE, this.sqlPath);
	}

	public String getSql() {
		return this.sqlPath;
	}

	public void setPostgres(String connectionString) {
		if (connectionString == null) return;
		this.postgresConnectionString = connectionString;
		compileParametersParsed.put(PostgresConnection.INSTANCE, this.postgresConnectionString);
	}

	public String getPostgres() {
		return this.postgresConnectionString;
	}

	public void setOracle(String connectionString) {
		if (connectionString == null) return;
		this.oracleConnectionString = connectionString;
		compileParametersParsed.put(OracleConnection.INSTANCE, this.oracleConnectionString);
	}

	public String getOracle() {
		return this.oracleConnectionString;
	}


	public void execute() throws MojoExecutionException, MojoFailureException {
		Utils.cleanupParameters(this.compileParametersParsed);
		// TODO: Default values
		Utils.sanitizeDirectories(this.compileParametersParsed);

		MojoContext context = new MojoContext(getLog())
				.with(this.flagsParsed)
				.with(this.compileParametersParsed)
				.with(Migration.INSTANCE)
				.with(Force.INSTANCE)
				.with(Download.INSTANCE)
				.with(Prompt.INSTANCE)
				.with(Settings.Option.SOURCE_ONLY);

		if (this.applySql) context.with(ApplyMigration.INSTANCE);

		List<CompileParameter> params = Main.initializeParameters(context, ".");

		if (!Main.processContext(context, params)) {
			throw new MojoExecutionException(context.errorLog.toString());
		}

		context.close();
	}
}
