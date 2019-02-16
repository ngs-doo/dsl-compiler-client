package com.dslplatform.sbt

import com.dslplatform.compiler.client.parameters.{DslCompiler, Settings, Targets}
import sbt.Keys._
import sbt._
import sbt.complete.Parsers

import scala.util.Try

object SbtDslPlatformPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin

  object autoImport {
    val dsl = taskKey[Seq[File]]("Compile DSL into appropriate targets (e.g. Scala sources)")
    val dslGenerate = taskKey[Seq[File]]("Compile DSL into appropriate targets (e.g. Scala sources)")
    val dslResource = inputKey[Seq[File]]("Scan code and create META-INF/services files for plugins")
    val dslMigrate = inputKey[Unit]("Create an SQL migration file based on difference from DSL in project and in the target database")
    val dslExecute = inputKey[Unit]("Execute custom DSL compiler command")

    val dslLibraries = settingKey[Map[Targets.Option, File]]("Compile libraries to specified outputs")
    val dslSources = settingKey[Map[Targets.Option, File]]("Generate sources to specified folders")
    val dslCompiler = settingKey[String]("Path to custom dsl-compiler.exe or port to running instance (requires .NET/Mono)")
    val dslServerMode = settingKey[Boolean]("Talk with DSL compiler in server mode (will be faster)")
    val dslServerPort = settingKey[Option[Int]]("Use a specific port to talk with DSL compiler in server mode")
    val dslPostgres = settingKey[String]("JDBC-like connection string to the Postgres database")
    val dslOracle = settingKey[String]("JDBC-like connection string to the Oracle database")
    val dslApplyMigration = settingKey[Boolean]("Apply SQL migration directly to the database")
    val dslNamespace = settingKey[String]("Root namespace for target language")
    val dslSettings = settingKey[Seq[Settings.Option]]("Additional compilation settings")
    val dslCustomSettings = settingKey[Seq[String]]("Custom compilation settings")
    val dslDslPath = settingKey[Seq[File]]("Path to DSL folder(s)")
    val dslResourcePath = settingKey[Option[File]]("Path to META-INF/services folder")
    val dslDependencies = settingKey[Map[Targets.Option, File]]("Library compilation requires various dependencies. Customize default paths to dependencies")
    val dslSqlPath = settingKey[File]("Output folder for SQL scripts")
    val dslLatest = settingKey[Boolean]("Check for latest versions (dsl-compiler, libraries, etc.)")
    val dslForce = settingKey[Boolean]("Force actions without prompt (destructive migrations, missing folders etc.)")
    val dslVerbose = settingKey[Boolean]("Add additional log messages")
    val dslAnsi = settingKey[Boolean]("Enable ANSI colours")
    val dslPlugins = settingKey[Option[File]]("Path to additional DSL plugins")
    val dslDownload = settingKey[Option[String]]("Download URL for a custom DSL compiler")
  }

  import autoImport._

  override lazy val projectSettings =
    inConfig(Compile)(baseDslSettings(Compile)) ++ Set(
      sourceGenerators in Compile += (dsl in Compile).taskValue
    ) ++
    inConfig(Test)(baseDslSettings(Test)) ++ Set(
      sourceGenerators in Test += (dsl in Test).taskValue
    )

  private def dslTempFolder = Def.setting {
    target.value / "dsl-temp"
  }

  private def createCompilerSettingsFingerprint(scope: Configuration,
                                                logger: Logger,
                                                dslCompiler: String,
                                                dslDownload: Option[String],
                                                dslTempFolder: File,
                                                dslSettings: Seq[Settings.Option],
                                                dslCustomSettings: Seq[String]): File = {
    def parsePort(in: String): Boolean = Try(Integer.parseInt(in)).filter(_ > 0).isSuccess

    lazy val fallBackCompiler = DslCompiler.lookupDefaultPath(new DslContext(Some(logger), false, false))

    val file =
      if (parsePort(dslCompiler) || dslCompiler.isEmpty) fallBackCompiler
      else {
        val customCompilerPath = new File(dslCompiler)
        if (!customCompilerPath.exists())
          logger.error(
            s"Unable to find the specified dslCompiler path: ${customCompilerPath.getAbsolutePath}")

        customCompilerPath
      }

    // The DSL fingerprint may be different for each scope
    val fingerprintFile = dslTempFolder / s"dsl-fingerprint-$scope.txt"
    val settings = {
      val values = dslSettings.map(_.name) ++ dslCustomSettings
      values.sorted.mkString("\n") + "\n" +
        (if (file.exists()) file.lastModified.toString else "") + "\n" +
        dslCompiler + "\n" +
        dslDownload.getOrElse("")
    }

    IO.write(fingerprintFile, settings)
    fingerprintFile
  }

  def baseDslSettings(scope: Configuration) = Seq(
    dsl := (dslGenerate in dsl).value,
    dslLibraries in dsl := Map.empty,
    dslSources in dsl := Map.empty,
    dslCompiler in dsl := "",
    dslServerMode in dsl := false,
    dslServerPort in dsl := Some(55662),
    dslPostgres in dsl := "",
    dslOracle in dsl := "",
    dslApplyMigration in dsl := false,
    dslNamespace in dsl := "",
    dslSettings in dsl := Nil,
    dslCustomSettings in dsl := Nil,
    dslDslPath in dsl := Seq(baseDirectory.value / "dsl"),
    dslDependencies in dsl := Map.empty,
    dslResourcePath in dsl := None,
    dslSqlPath in dsl := baseDirectory.value / "sql",
    dslLatest in dsl := true,
    dslForce in dsl := false,
    dslVerbose in dsl := false,
    dslAnsi in dsl := true,
    dslPlugins in dsl := Some(baseDirectory.value),
    dslDownload in dsl := None
  ) ++ inTask(dsl)(Seq(
    dslGenerate := {
      val logger = streams.value.log

      val depClassPath   = managedClasspath.value
      val tempFolder     = dslTempFolder.value
      val cacheDirectory = streams.value.cacheDirectory
      val settings       = dslSettings.value

      if (dslSources.value.isEmpty) Seq()
      else {
        val cached = FileFunction.cached(
          cacheDirectory / "dsl-generate",
          inStyle = FilesInfo.hash,
          outStyle = FilesInfo.hash
        ) { changes: Set[File] =>
          if (changes.nonEmpty) logger.info("Re-compiling DSL files...")

          dslSources.value.toList.flatMap { case (targetArg, targetOutput) =>
            Actions.generateSource(
              logger,
              dslVerbose.value,
              dslAnsi.value,
              targetArg,
              targetOutput,
              dslDslPath.value,
              dslPlugins.value,
              dslCompiler.value,
              dslServerMode.value,
              dslDownload.value,
              dslServerPort.value,
              dslNamespace.value,
              dslSettings.value,
              dslCustomSettings.value,
              depClassPath,
              dslLatest.value
            ).map(_.getAbsoluteFile)
          }.toSet
        }

        // Index only *.dsl and *.ddd files
        val dslPathFiles = dslDslPath
          .value
          .flatMap(path => (path ** ("*.dsl" || "*.ddd")).get)
          .toSet

        logger.info(s"Found ${dslPathFiles.size} DSL files")
        val settingsFile = createCompilerSettingsFingerprint(
          scope, logger, dslCompiler.value, dslDownload.value, tempFolder, settings, dslCustomSettings.value)
        cached(dslPathFiles + settingsFile).toSeq
      }
    },

    dslResource := {
      if (dslResourcePath.value.isEmpty) {
        streams.value.log.error(s"$scope: dslResourcePath must be set")
        Seq()
      } else {
        val targets = {
          val defined = (dslSources.value.keys ++ dslLibraries.value.keys).toSet
          if (defined.isEmpty) {
            streams.value.log.warn(s"$scope: dslSources and/or dslLibraries are not set. Assuming revenj.scala as target")
            Set(Targets.Option.REVENJ_SCALA)
          } else defined
        }

        targets.flatMap { tgt =>
          Actions.generateResources(
            streams.value.log,
            tgt,
            dslResourcePath.value.get,
            Seq((target in scope).value),
            (fullClasspath in scope).value)
        }.toSeq
      }
    },

    dslMigrate := {
      if (dslPostgres.value.isEmpty && dslOracle.value.isEmpty)
        streams.value.log.error(s"$scope: JDBC connection string not defined for PostgreSQL or Oracle")
      else {
        val jdbcs =
          (if (dslPostgres.value.nonEmpty) List(dslPostgres.value) else List()) ++
          (if (dslOracle.value.nonEmpty) List(dslOracle.value) else List())

        jdbcs.foreach { jdbc =>
          Actions.dbMigration(
            streams.value.log,
            dslVerbose.value,
            dslAnsi.value,
            jdbc,
            dslPostgres.value.nonEmpty,
            dslSqlPath.value,
            dslDslPath.value,
            dslPlugins.value,
            dslCompiler.value,
            dslServerMode.value,
            dslDownload.value,
            dslServerPort.value,
            dslApplyMigration.value,
            dslForce.value,
            dslLatest.value)
        }
      }
    },

    dslExecute := Def.inputTask {
      Actions.execute(
        streams.value.log,
        dslVerbose.value,
        dslAnsi.value,
        dslDslPath.value,
        dslPlugins.value,
        dslCompiler.value,
        dslServerMode.value,
        dslDownload.value,
        dslServerPort.value,
        Parsers.spaceDelimited("<arg>").parsed)
    }
  ))
}
