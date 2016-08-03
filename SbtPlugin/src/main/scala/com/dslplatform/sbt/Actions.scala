package com.dslplatform.sbt

import java.io.{File, FileOutputStream}
import java.net.{URLClassLoader, URLEncoder}
import java.nio.file.{Files, Path, StandardCopyOption}

import com.dslplatform.compiler.client.Main
import com.dslplatform.compiler.client.parameters.{Settings, _}
import net.revenj.patterns.DomainEventHandler
import org.clapper.classutil.ClassFinder
import sbt.Logger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Actions {
  def findTarget(logger: Logger, name: String): Targets.Option = {
    Targets.Option.values().find(it => it.toString.equals(name)) match {
      case Some(target) => target
      case _ =>
        logger.error("Unable to find target: " + name)
        logger.error("List of known targets: ")
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
                     latest: Boolean = true): File = {
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
    output
  }

  def generateSource(logger: Logger,
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
      output.delete()
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
    logger.info("Source for " + target + " created in " + output.getPath)
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
    ctx.put(Migration.INSTANCE, "")
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

  def scanEventHandlers(logger: Logger, classes: File, manifests: File, classLoader: Option[ClassLoader] = None): Seq[File] = {
    logger.info("Scanning for events in " + classes.getAbsolutePath)
    val folders = new scala.collection.mutable.HashSet[File]
    val implementations =
      ClassFinder(Seq(classes)).getClasses()
        .withFilter(it => it.isConcrete && it.implements("net.revenj.patterns.DomainEventHandler"))
        .map(_.name)
    gatherSubfolders(classes, folders)
    val loader = new URLClassLoader(folders.map(_.toURI.toURL).toArray, classLoader.getOrElse(classOf[DomainEventHandler[_]].getClassLoader))
    val handlers = new mutable.HashMap[String, ArrayBuffer[String]]()
    implementations foreach { name =>
      try {
        val manifest = Class.forName(name, false, loader)
        manifest.getGenericInterfaces.filter(_.getTypeName.startsWith("net.revenj.patterns.DomainEventHandler<")).foreach { m =>
          val handler = handlers.getOrElseUpdate(URLEncoder.encode(m.getTypeName, "UTF-8"), new ArrayBuffer[String]())
          handler += name
        }
      } catch {
        case x: Throwable =>
          logger.error("unable to load " + name + ". error: " + x)
      }
    }
    loader.close()
    val oldServices = manifests.listFiles().filter(_.getName.startsWith("net.revenj.patterns.DomainEventHandler%"))
    oldServices foreach { _.delete() }
    if (handlers.nonEmpty) {
      logger.info("Saving manifests to " + manifests.getAbsolutePath)
      handlers foreach { case (k, vals) =>
        val file = new File(manifests, k)
        if (!file.getParentFile.exists() && !file.getParentFile.mkdirs()) {
          logger.error("Error creating folder: " + file.getParentFile.getAbsolutePath)
        }
        val fos = new FileOutputStream(file)
        fos.write(vals.mkString("\n").getBytes("UTF-8"))
        fos.close()
      }
    }
    handlers.keySet.map(k => new File(manifests, k)).toSeq
  }
}
