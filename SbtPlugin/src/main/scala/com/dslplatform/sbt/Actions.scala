package com.dslplatform.sbt

import java.io.File

import com.dslplatform.compiler.client.Main
import com.dslplatform.compiler.client.parameters.{Settings, _}
import sbt.Logger

private[sbt] object Actions {
  def findTarget(logger: Logger, name: String): Targets.Option = {
    Targets.Option.values().find(it => it.toString.equals(name)) match {
      case Some(target) => target
      case _ =>
        logger.error("Unable to find target: " + name)
        logger.error("List of known targets:")
        Targets.Option.values() foreach { it => logger.error(it.toString) }
        throw new RuntimeException("Unable to find target: " + name)
    }
  }

  def compileLibrary(logger: Logger,
                     target: Targets.Option,
                     output: File,
                     dsl: File,
                     plugins: Option[File] = None,
                     compiler: String = "",
                     namespace: String = "",
                     settings: Seq[Settings.Option] = Nil,
                     dependencies: Option[File] = None,
                     latest: Boolean = true): Unit = {
    val ctx = new DslContext(logger)
    ctx.put(target.toString, output.getAbsolutePath)
    if (namespace.nonEmpty) {
      ctx.put(Namespace.INSTANCE, namespace)
    }
    settings.foreach(it => ctx.put(it.toString, ""))
    if (dependencies.isDefined) {
      ctx.put("dependency:" + target.toString, dependencies.get.getAbsolutePath)
    }
    executeContext(dsl, compiler, plugins, latest, ctx)
  }

  def generateSource(logger: Logger,
                     target: Targets.Option,
                     output: File,
                     dsl: File,
                     plugins: Option[File] = None,
                     compiler: String = "",
                     namespace: String = "",
                     settings: Seq[Settings.Option] = Nil,
                     latest: Boolean = true): Unit = {
    if (output.exists()) {
      output.listFiles().foreach(_.delete())
    } else {
      output.mkdirs()
    }
    val ctx = new DslContext(logger)
    ctx.put(Settings.Option.SOURCE_ONLY.toString, "")
    ctx.put(target.toString, "")
    if (namespace.nonEmpty) {
      ctx.put(Namespace.INSTANCE, namespace)
    }
    settings.foreach(it => ctx.put(it.toString, ""))
    executeContext(dsl, compiler, plugins, latest, ctx)
    val generated = new File(TempPath.getTempProjectPath(ctx), target.name)
    generated.listFiles().foreach(it => it.renameTo(new File(output, it.getName)))
    logger.info("Source for " + target + " created in " + output.getPath)
  }

  def dbMigration(logger: Logger,
                  jdbcUrl: String,
                  postgres: Boolean = true,
                  output: File,
                  dsl: File,
                  plugins: Option[File] = None,
                  compiler: String = "",
                  apply: Boolean = false,
                  force: Boolean = false,
                  latest: Boolean = true): Unit = {
    val ctx = new DslContext(logger)
    if (postgres) ctx.put(PostgresConnection.INSTANCE, jdbcUrl) else ctx.put(OracleConnection.INSTANCE, jdbcUrl)
    if (apply) ctx.put(ApplyMigration.INSTANCE, "")
    if (force) ctx.put(Force.INSTANCE, "")
    ctx.put(SqlPath.INSTANCE, output.getPath)
    executeContext(dsl, compiler, plugins, latest, ctx)
  }

  def execute(logger: Logger,
              dsl: File,
              plugins: Option[File] = None,
              compiler: String = "",
              arguments: Seq[String]): Unit = {
    val ctx = new DslContext(logger)
    for (a <- arguments) {
      val cmd = if (a.startsWith("-") || a.startsWith("/")) a.substring(1) else a
      val eqInd = cmd.indexOf("=")
      if (eqInd == -1) {
        ctx.put(cmd, null)
      } else {
        ctx.put(cmd.substring(0, eqInd), cmd.substring(eqInd))
      }
    }
    executeContext(dsl, compiler, plugins, latest = false, ctx)
  }

  private def executeContext(dsl: File, compiler: String, plugins: Option[File], latest: Boolean, ctx: DslContext): Unit = {
    ctx.put(DslPath.INSTANCE, dsl.getPath)
    if (compiler.nonEmpty) {
      ctx.put(DslCompiler.INSTANCE, compiler)
    }
    if (latest) {
      ctx.put(Download.INSTANCE, "")
    }
    val params = Main.initializeParameters(ctx, plugins.getOrElse(new File(".")).getPath)
    Main.processContext(ctx, params)
  }
}
