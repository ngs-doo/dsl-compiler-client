package com.dslplatform.sbt

import java.io.{File, FileOutputStream}
import java.lang.management.ManagementFactory
import java.net._
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.util

import com.dslplatform.compiler.client.{CompileParameter, Main, Utils}
import com.dslplatform.compiler.client.parameters.{Settings, _}
import org.clapper.classutil.ClassFinder
import sbt.Def.Classpath
import sbt.{Attributed, File, IO, Logger}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object Actions {

  private case class ServerInfo(port: Int, process: Option[Process])

  private var serverInfo: Option[ServerInfo] = None

  private def trySocket(port: Int): Option[Socket] = {
    val isIp4 = InetAddress.getLoopbackAddress.isInstanceOf[Inet4Address]
    try {
      Some(new Socket(if (isIp4) "127.0.0.1" else "::1", port))
    } catch {
      case _: Throwable =>
        try {
          Some(new Socket(if (isIp4) "::1" else "127.0.0.1", port))
        } catch {
          case _: Throwable =>
            None
        }
    }
  }

  def setupServerMode(compiler: String, logger: Option[Logger], verbose: Boolean, ansi: Boolean, url: Option[String], port: Option[Int]): Unit = {
    if (serverInfo.isEmpty) {
      try {
        val livePort = {
          if (port.nonEmpty) {
            trySocket(port.get) match {
              case Some(socket) =>
                try {
                  socket.close()
                } catch {
                  case _: Throwable =>
                }
                port
              case _ =>
                None
            }
          } else None
        }
        if (livePort.isDefined) {
          serverInfo = Some(ServerInfo(livePort.get, None))
        } else {
          val path = {
            if (compiler == null || compiler.isEmpty) {
              logger.foreach(_.info("Downloading latest DSL compiler since compiler path is not specified."))
              val downloadCtx = new DslContext(logger, verbose, ansi)
              downloadCtx.put(Download.INSTANCE, url.getOrElse(""))
              if (!Main.processContext(downloadCtx, util.Arrays.asList[CompileParameter](Download.INSTANCE, DslCompiler.INSTANCE))) {
                logger.foreach(_.warn("Unable to setup DSL Platform client"))
              }
              downloadCtx.get(DslCompiler.INSTANCE)
            } else compiler
          }
          if (path == null || path.isEmpty) {
            logger.foreach(_.error("Unable to setup dsl-compiler.exe. Please check if Mono/.NET is installed and available on path."))
          } else {
            if (new File(path).exists()) {
              val rnd = new Random
              val value = port.getOrElse(40000 + rnd.nextInt(20000))
              logger.foreach(_.info(s"Starting DSL Platform compiler found at: $path on port: $value"))
              val serverCtx = new DslContext(logger, verbose, ansi)
              val process = startServerMode(serverCtx, path, value)
              serverInfo = Some(ServerInfo(value, Some(process)))
            } else {
              logger.foreach(_.error(s"Unable to find specified dsl-compiler at $path"))
            }
          }
        }
      } catch {
        case ex: Throwable =>
          logger.foreach(_.error(ex.getMessage))
      }
    }
  }

  private def startServerMode(context: DslContext, compiler: String, port: Int): Process = {
    val arguments = new util.ArrayList[String]
    arguments.add(compiler)
    arguments.add("server-mode")
    arguments.add(s"port=$port")
    try {
      if (InetAddress.getLoopbackAddress.isInstanceOf[Inet4Address]) {
        arguments.add("ip=v4")
      }
    } catch {
      case _: UnknownHostException =>
    }
    try {
      val procId = ManagementFactory.getRuntimeMXBean.getName.split("@")(0)
      arguments.add(s"parent=$procId")
    } catch {
      case _: Exception =>
    }
    if (!Utils.isWindows) {
      val mono = Mono.findMono(context)
      if (mono.isSuccess) arguments.add(0, mono.get)
      else throw new RuntimeException("Mono is required to run DSL compiler. Mono not detected or specified.")
    }
    val pb = new ProcessBuilder(arguments)
    pb.start
  }

  private def stopServerMode(logger: Option[Logger]): Unit = {
    serverInfo match {
      case Some(si) if si.process.isDefined =>
        try {
          si.process.get.destroy()
        } catch {
          case ex: Throwable =>
            logger.foreach(_.error(ex.getMessage))
        }
      case _ =>
    }
    serverInfo = None
  }

  def compileLibrary(
    logger: Logger,
    verbose: Boolean,
    ansi: Boolean,
    target: Targets.Option,
    output: File,
    dsl: Seq[File],
    plugins: Option[File] = None,
    compiler: String = "",
    serverMode: Boolean = false,
    serverURL: Option[String] = None,
    serverPort: Option[Int] = None,
    namespace: String = "",
    settings: Seq[Settings.Option] = Nil,
    customSettings: Seq[String] = Nil,
    dependencies: Option[File] = None,
    classPath: Classpath,
    latest: Boolean = true): File = {

    val ctx = new DslContext(Some(logger), verbose, ansi)
    ctx.put(target.toString, output.getAbsolutePath)
    addVersionAndSettings(ctx, target, classPath, settings, customSettings, namespace)
    if (dependencies.isDefined) {
      ctx.put(s"dependency:$target", dependencies.get.getAbsolutePath)
      executeContext(dsl, compiler, serverMode, serverURL, serverPort, plugins, latest, ctx, logger, verbose, ansi)
    } else {
      val tmpFolder = Files.createTempDirectory("dsl-clc")
      try {
        classPath foreach { it =>
          ctx.log(s"Copying ${it.data} to $tmpFolder")
          Files.copy(it.data.toPath, new File(tmpFolder.toFile, it.data.getName).toPath)
        }
        ctx.put(s"dependency:$target", tmpFolder.toFile.getAbsolutePath)
        executeContext(dsl, compiler, serverMode, serverURL, serverPort, plugins, latest, ctx, logger, verbose, ansi)
      } finally {
        try {
          if (!tmpFolder.toFile.delete()) {
            ctx.log(s"Failed to delete ${tmpFolder.toFile}")
            tmpFolder.toFile.deleteOnExit()
          }
        } catch {
          case _: Throwable =>
        }
      }
    }
    output
  }

  def generateSource(
    logger: Logger,
    verbose: Boolean,
    ansi: Boolean,
    target: Targets.Option,
    output: File,
    dsl: Seq[File],
    plugins: Option[File] = None,
    compiler: String = "",
    serverMode: Boolean = false,
    serverURL: Option[String] = None,
    serverPort: Option[Int] = None,
    namespace: String = "",
    settings: Seq[Settings.Option] = Nil,
    customSettings: Seq[String] = Nil,
    classPath: Classpath,
    latest: Boolean = true): Seq[File] = {
    val cwd = Paths.get("").toAbsolutePath.toString
    if (cwd == output.getCanonicalPath || !output.getCanonicalPath.startsWith(cwd)) {
      logger.error("Output path must be at least one level below working directory")
      return Seq.empty
    }

    if (!output.exists()) {
      if (!output.mkdirs()) {
        logger.warn(s"Failed creating output folder: ${output.getAbsolutePath}")
      }
    } else {
      output.listFiles() foreach { f =>
        IO.delete(f)
        if (f.exists()) {
          logger.warn(s"Failed to delete: ${f.getAbsolutePath}")
        }
      }
    }

    val ctx = new DslContext(Some(logger), verbose, ansi)
    ctx.put(Settings.Option.SOURCE_ONLY.toString, "")
    ctx.put(target.toString, "")

    val tempFolder = IO.createTemporaryDirectory
    ctx.put(s"source:$target", tempFolder.getAbsolutePath)

    addVersionAndSettings(ctx, target, classPath, settings, customSettings, namespace)
    executeContext(dsl, compiler, serverMode, serverURL, serverPort, plugins, latest, ctx, logger, verbose, ansi)
    val generated = new File(tempFolder, target.name)
    val files = new ArrayBuffer[File]()
    deepCopy(generated.toPath, output.toPath, files)
    IO.delete(tempFolder)
    logger.info(s"Source for $target created in ${output.getPath}")
    files.result()
  }

  private def addVersionAndSettings(
    ctx: DslContext,
    target: Targets.Option,
    classPath: Classpath,
    settings: Seq[Settings.Option],
    customSettings: Seq[String],
    namespace: String
  ) = {
    val version = classPath.find { d =>
      d.data.getAbsolutePath.contains("revenj-core")
    }
    version.foreach { v =>
      val parts = v.data.getAbsolutePath.split("\\/".toCharArray)
      val ind = parts.indexOf(parts.find(_.contains("revenj-core")).getOrElse(""))
      if (ind < parts.length - 1) {
        ctx.put(s"library:$target", parts(ind + 1))
      }
    }
    if (namespace.nonEmpty) {
      ctx.put(Namespace.INSTANCE, namespace)
    }
    settings.foreach(it => ctx.put(it.toString, ""))
    if (customSettings.nonEmpty) {
      ctx.put(Settings.INSTANCE, customSettings.mkString(","))
      ctx.put(Force.INSTANCE, "")
    }
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
      scanPlugins(logger, folders, manifests, "net.revenj.patterns.DomainEventHandler", dependencies) ++
        scanPlugins(logger, folders, manifests, "net.revenj.patterns.AggregateDomainEventHandler", dependencies) ++
        scanPlugins(logger, folders, manifests, "net.revenj.patterns.ReportAspect", dependencies) ++
        scanPlugins(logger, folders, manifests, "net.revenj.patterns.PersistableRepositoryAspect", dependencies) ++
        scanPlugins(logger, folders, manifests, "net.revenj.patterns.EventStoreAspect", dependencies) ++
        Seq(scanPlugins(logger, folders, manifests, "net.revenj.server.handlers.RequestBinding")) ++
        Seq(scanPlugins(logger, folders, manifests, "net.revenj.server.ServerCommand")) ++
        Seq(scanPlugins(logger, folders, manifests, "net.revenj.extensibility.SystemAspect"))
    } else if (target == Targets.Option.REVENJ_JAVA || target == Targets.Option.REVENJ_JAVA_POSTGRES
      || target == Targets.Option.REVENJ_SPRING) {
      scanPlugins(logger, folders, manifests, "org.revenj.patterns.DomainEventHandler", dependencies) ++
        Seq(scanPlugins(logger, folders, manifests, "org.revenj.extensibility.SystemAspect"))
    } else {
      Nil
    }
  }

  def dbMigration(
    logger: Logger,
    verbose: Boolean,
    ansi: Boolean,
    jdbcUrl: String,
    postgres: Boolean = true,
    output: File,
    dsl: Seq[File],
    plugins: Option[File] = None,
    compiler: String = "",
    serverMode: Boolean = false,
    serverURL: Option[String],
    serverPort: Option[Int] = None,
    apply: Boolean = false,
    force: Boolean = false,
    latest: Boolean = true): Unit = {

    val ctx = new DslContext(Some(logger), verbose, ansi)
    if (postgres) ctx.put(PostgresConnection.INSTANCE, jdbcUrl) else ctx.put(OracleConnection.INSTANCE, jdbcUrl)
    if (apply) ctx.put(ApplyMigration.INSTANCE, "")
    if (force) ctx.put(Force.INSTANCE, "")
    ctx.put(SqlPath.INSTANCE, output.getPath)
    ctx.put(Migration.INSTANCE, "")
    executeContext(dsl, compiler, serverMode, serverURL, serverPort, plugins, latest, ctx, logger, verbose, ansi)
  }

  def execute(
    logger: Logger,
    verbose: Boolean,
    ansi: Boolean,
    dsl: Seq[File],
    plugins: Option[File] = None,
    compiler: String = "",
    serverMode: Boolean = false,
    serverURL: Option[String],
    serverPort: Option[Int] = None,
    arguments: Seq[String]): Unit = {

    val ctx = new DslContext(Some(logger), verbose, ansi)
    for (a <- arguments) {
      val cmd = if (a.startsWith("-") || a.startsWith("/")) a.substring(1) else a
      val eqInd = cmd.indexOf("=")
      if (eqInd == -1) {
        ctx.put(cmd, null)
      } else {
        ctx.put(cmd.substring(0, eqInd), cmd.substring(eqInd + 1))
      }
    }
    executeContext(dsl, compiler, serverMode, serverURL, serverPort, plugins, latest = false, ctx, logger, verbose, ansi)
  }

  private def executeContext(dsls: Seq[File], compiler: String, serverMode: Boolean, serverURL: Option[String], serverPort: Option[Int], plugins: Option[File], latest: Boolean, ctx: DslContext, logger: Logger, verbose: Boolean, ansi: Boolean): Unit = {
    if (dsls.isEmpty) {
      throw new RuntimeException(s"No DSL paths/files specified in dslDslPath setting")
    }
    ctx.put(DslPath.INSTANCE, dsls.mkString(File.pathSeparator))
    val startedNow = {
      if (serverMode) {
        val info = serverInfo
        if (info.flatMap(_.process).exists(t => !t.isAlive)) {
          logger.warn("Dead DSL Platform process detected. Will try restart...")
          val port = info.get.port
          stopServerMode(Some(logger))
          setupServerMode(compiler, Some(logger), verbose, ansi, serverURL, Some(port))
          true
        } else if (info.isEmpty) {
          setupServerMode(compiler, Some(logger), verbose, ansi, serverURL, serverPort)
          true
        } else false
      } else false
    }
    (serverMode, serverInfo) match {
      case (true, Some(info)) =>
        ctx.put(DslCompiler.INSTANCE, info.port.toString)
      case (true, _) =>
        logger.warn("DSL Platform server mode specified, but server not running. Will try process invocation")
        if (compiler.nonEmpty) {
          ctx.put(DslCompiler.INSTANCE, compiler)
        }
      case _ =>
        if (compiler.nonEmpty && (!serverMode || serverInfo.isEmpty)) {
          ctx.put(DslCompiler.INSTANCE, compiler)
        }
    }
    if (!serverMode && latest) {
      ctx.put(Download.INSTANCE, serverURL.getOrElse(""))
    }
    val params = Main.initializeParameters(ctx, plugins.getOrElse(new File(".")).getPath)
    if (!Main.processContext(ctx, params) && !ctx.isParseError && !ctx.hasInteracted) {
      (serverMode, serverInfo) match {
        case (true, Some(info)) =>
        logger.warn("Will retry DSL compilation without server mode...")
        ctx.put(DslCompiler.INSTANCE, if (compiler.nonEmpty) compiler else "")
        Main.processContext(ctx, params)
        if (!startedNow) {
          tryRestart(logger, verbose, ansi, info, compiler, serverURL)
        }
        case _ =>
      }
    }
  }

  private def tryRestart(logger: Logger, verbose: Boolean, ansi: Boolean, info: ServerInfo, compiler: String, serverURL: Option[String]): Unit = {
    logger.warn("Checking DSL Platform server state and trying restart...")
    trySocket(info.port) match {
      case Some(socket) =>
        try {
          logger.warn(s"Shutting down server on ${info.port}")
          socket.getOutputStream.write("shutdown\n".getBytes("UTF-8"))
          socket.close()
        } catch {
          case ex: Throwable =>
            logger.warn(s"Error shutting down DSL Platform server: ${ex.getMessage}")
        }
        stopServerMode(Some(logger))
      case _ =>
        logger.warn(s"DSL Platform server not responding on ${info.port}")
        stopServerMode(Some(logger))
        setupServerMode(compiler, Some(logger), verbose, ansi, serverURL, Some(info.port))
    }
  }

  private def gatherSubfolders(file: File, subfolders: mutable.HashSet[File]): Unit = {
    subfolders.add(file)
    file.listFiles.filter(_.isDirectory) foreach { f =>
      if (subfolders.add(f)) {
        gatherSubfolders(f, subfolders)
      }
    }
  }

  private def scanPlugins(logger: Logger, folders: Seq[File], manifests: File, target: String, dependencies: Classpath): Seq[File] = {
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
    val loader = new URLClassLoader(urls.map(_.toURI.toURL).toArray, Thread.currentThread().getContextClassLoader)
    val handlers = new mutable.HashMap[String, ArrayBuffer[String]]()
    implementations foreach { name =>
      try {
        logger.debug(s"Loading: $name")
        val manifest = Class.forName(name, false, loader)
        manifest.getGenericInterfaces.filter(_.getTypeName.startsWith(s"$target<")).foreach { m =>
          val handler = handlers.getOrElseUpdate(URLEncoder.encode(m.getTypeName.replace(" ", ""), "UTF-8"), new ArrayBuffer[String]())
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

  private def scanPlugins(logger: Logger, folders: Seq[File], manifests: File, target: String): File = {
    logger.info(s"""Scanning for $target plugins in ${folders.mkString(", ")}""")
    val implementations =
      ClassFinder(folders).getClasses()
        .withFilter(it => it.isConcrete && it.implements(target))
        .map(_.name)
        .distinct
    logger.debug(s"""Number of matching implementations: ${implementations.size}""")
    if (manifests.exists()) {
      val oldServices = manifests.listFiles().filter(_.getName.startsWith(s"$target%"))
      oldServices foreach {
        _.delete()
      }
    }
    logger.info(s"Saving manifests to ${manifests.getAbsolutePath}")
    val file = new File(manifests, target)
    if (!file.getParentFile.exists() && !file.getParentFile.mkdirs()) {
      logger.error(s"Error creating folder: ${file.getParentFile.getAbsolutePath}")
    }
    val fos = new FileOutputStream(file)
    fos.write(implementations.mkString("\n").getBytes("UTF-8"))
    fos.close()
    file
  }
}
