package com.dslplatform.sbt

import java.io.{File, FileOutputStream}
import java.net.{URLClassLoader, URLEncoder}
import java.nio.file.{Files, Path, StandardCopyOption}

import com.dslplatform.compiler.client.Main
import com.dslplatform.compiler.client.parameters.{Settings, _}
import org.clapper.classutil.ClassFinder
import sbt.Def.Classpath
import sbt.Logger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Actions {
  def compileLibrary(
    logger: Logger,
    target: Targets.Option,
    output: File,
    dsl: File,
    plugins: Option[File] = None,
    compiler: String = "",
    namespace: String = "",
    settings: Seq[Settings.Option] = Nil,
    dependencies: Option[File] = None,
    latest: Boolean = true): File = {

    val ctx = new DslContext(logger)
    ctx.put(target.toString, output.getAbsolutePath)
    if (namespace.nonEmpty) {
      ctx.put(Namespace.INSTANCE, namespace)
    }
    settings.foreach(it => ctx.put(it.toString, ""))
    if (dependencies.isDefined) {
      ctx.put(s"dependency:$target", dependencies.get.getAbsolutePath)
    }
    executeContext(dsl, compiler, plugins, latest, ctx)
    output
  }

  def generateSource(
    logger: Logger,
    target: Targets.Option,
    output: File,
    dsl: File,
    plugins: Option[File] = None,
    compiler: String = "",
    namespace: String = "",
    settings: Seq[Settings.Option] = Nil,
    latest: Boolean = true): Seq[File] = {
    //TODO: remove only non-existing files
    if (output.exists()) {
      if (!output.delete()) {
        logger.warn(s"Failed to delete output folder: ${output.getAbsolutePath}")
      }
    }
    output.mkdirs()
    val ctx = new DslContext(logger)
    ctx.put(Settings.Option.SOURCE_ONLY.toString, "")
    ctx.put(target.toString, "")
    if (namespace.nonEmpty) {
      ctx.put(Namespace.INSTANCE, namespace)
    }
    settings.foreach(it => ctx.put(it.toString, ""))
    executeContext(dsl, compiler, plugins, latest, ctx)
    val generated = new File(TempPath.getTempProjectPath(ctx), target.name)
    //TODO: copy only changed/new files
    val files = new ArrayBuffer[File]()
    deepCopy(generated.toPath, output.toPath, files)
    logger.info(s"Source for $target created in ${output.getPath}")
    files.result()
  }

  private def deepCopy(from: Path, to: Path, files: ArrayBuffer[File]): Unit = {
    if (from.toFile.isDirectory) {
      if (!to.toFile.exists()) {
        to.toFile.mkdirs()
      }
      from.toFile.list foreach { it =>
        val source = new File(from.toFile, it)
        val target = new File(to.toFile, it)
        deepCopy(source.toPath, target.toPath, files)
      }
    } else {
      Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
      files += to.toFile
    }
  }

  def generateResources(
    logger: Logger,
    target: Targets.Option,
    manifests: File,
    folders: Seq[File],
    dependencies: Classpath): Seq[File] = {
    if (!manifests.exists()) {
      logger.warn(s"Specified META-INF/services does not exist. Creating one in: ${manifests.getAbsolutePath}")
      if (!manifests.mkdirs()) {
        logger.error(s"Failed to create META-INF/services folder in: ${manifests.getAbsolutePath}")
      }
    }
    if (target == Targets.Option.REVENJ_SCALA || target == Targets.Option.REVENJ_SCALA_POSTGRES) {
      scanEventHandlers(logger, folders, manifests, "net.revenj.patterns.DomainEventHandler", dependencies)
    } else if (target == Targets.Option.REVENJ_JAVA || target == Targets.Option.REVENJ_JAVA_POSTGRES
      || target == Targets.Option.REVENJ_SPRING) {
      scanEventHandlers(logger, folders, manifests, "org.revenj.patterns.DomainEventHandler", dependencies)
    } else {
      Nil
    }
  }

  def dbMigration(
    logger: Logger,
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
    ctx.put(Migration.INSTANCE, "")
    executeContext(dsl, compiler, plugins, latest, ctx)
  }

  def execute(
    logger: Logger,
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
        ctx.put(cmd.substring(0, eqInd), cmd.substring(eqInd + 1))
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

  private def gatherSubfolders(file: File, subfolders: mutable.HashSet[File]) : Unit = {
    subfolders.add(file)
    file.listFiles.filter(_.isDirectory) foreach { f =>
      if (subfolders.add(f)) {
        gatherSubfolders(f, subfolders)
      }
    }
  }

  private def scanEventHandlers(logger: Logger, folders: Seq[File], manifests: File, target: String, dependencies: Classpath): Seq[File] = {
    logger.info(s"""Scanning for $target events in ${folders.mkString(", ")}""")
    val implementations =
      ClassFinder(folders).getClasses()
        .withFilter(it => it.isConcrete && it.implements(target))
        .map(_.name)
        .distinct
    logger.debug(s"""Number of matching implementations: ${implementations.size}""")
    val urls = new scala.collection.mutable.HashSet[File]
    folders foreach { f => gatherSubfolders(f, urls) }
    urls ++= dependencies.map(_.data)
    val loader = new URLClassLoader(urls.map(_.toURI.toURL).toArray)
    val handlers = new mutable.HashMap[String, ArrayBuffer[String]]()
    implementations foreach { name =>
      try {
        logger.debug(s"Loading: $name")
        val manifest = Class.forName(name, false, loader)
        manifest.getGenericInterfaces.filter(_.getTypeName.startsWith(s"$target<")).foreach { m =>
          val handler = handlers.getOrElseUpdate(URLEncoder.encode(m.getTypeName, "UTF-8"), new ArrayBuffer[String]())
          handler += name
        }
      } catch {
        case ex: Throwable =>
          logger.error(s"unable to load $name. Error: $ex")
      }
    }
    loader.close()
    if (manifests.exists()) {
      val oldServices = manifests.listFiles().filter(_.getName.startsWith(s"$target%"))
      oldServices foreach {
        _.delete()
      }
    }
    if (handlers.nonEmpty) {
      logger.info(s"Saving manifests to ${manifests.getAbsolutePath}")
      handlers foreach { case (k, vals) =>
        val file = new File(manifests, k)
        if (!file.getParentFile.exists() && !file.getParentFile.mkdirs()) {
          logger.error(s"Error creating folder: ${file.getParentFile.getAbsolutePath}")
        }
        val fos = new FileOutputStream(file)
        fos.write(vals.mkString("\n").getBytes("UTF-8"))
        fos.close()
      }
    }
    handlers.keySet.map(k => new File(manifests, k)).toSeq
  }
}
