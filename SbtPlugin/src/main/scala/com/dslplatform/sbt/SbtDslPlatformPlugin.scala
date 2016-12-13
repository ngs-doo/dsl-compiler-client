package com.dslplatform.sbt

import sbt._
import Keys._
import com.dslplatform.compiler.client.parameters.Targets
import com.dslplatform.compiler.client.parameters.Settings
import sbt.complete.Parsers

import scala.collection.mutable.ArrayBuffer

object SbtDslPlatformPlugin extends AutoPlugin {

  object autoImport {
    val dslLibrary = inputKey[Unit]("Compile DSL into a compiled jar ready for usage.")
    val dslSource = inputKey[Seq[File]]("Compile DSL into generated source ready for usage.")
    val dslResource = inputKey[Seq[File]]("Scan code and create META-INF/services files for plugins.")
    val dslMigrate = inputKey[Unit]("Create an SQL migration file based on difference from DSL in project and in the target database.")
    val dslExecute = inputKey[Unit]("Execute custom DSL compiler command")

    val dslLibraries = settingKey[Map[Targets.Option, File]]("Compile libraries to specified outputs)")
    val dslSources = settingKey[Map[Targets.Option, File]]("Generate sources to specified folders)")
    val dslCompiler = settingKey[String]("Path to custom dsl-compiler.exe or port to running instance (requires .NET/Mono)")
    val dslServerMode = settingKey[Boolean]("Talk with DSL compiler in server mode (will be faster)")
    val dslServerPort = settingKey[Option[Int]]("Use a specific port to talk with DSL compiler in server mode")
    val dslPostgres = settingKey[String]("JDBC-like connection string to the Postgres database")
    val dslOracle = settingKey[String]("JDBC-like connection string to the Oracle database")
    val dslApplyMigration = settingKey[Boolean]("Apply SQL migration directly to the database")
    val dslNamespace = settingKey[String]("Root namespace for target language")
    val dslSettings = settingKey[Seq[Settings.Option]]("Additional compilation settings")
    val dslDslPath = settingKey[File]("Path to DSL folder")
    val dslResourcePath = settingKey[File]("Path to META-INF/services folder")
    val dslDependencies = settingKey[Map[Targets.Option, File]]("Library compilation requires various dependencies. Customize default paths to dependencies")
    val dslSqlPath = settingKey[File]("Output folder for SQL scripts")
    val dslLatest = settingKey[Boolean]("Check for latest versions (dsl-compiler, libraries, etc...)")
    val dslForce = settingKey[Boolean]("Force actions without prompt (destructive migrations, missing folders, etc...)")
    val dslPlugins = settingKey[Option[File]]("Path to additional DSL plugins")
  }

  import autoImport._

  lazy val DslPlatform = config("dsl-platform") extend(Compile)

  override def projectConfigurations: Seq[Configuration] = Seq(DslPlatform)

  private def findTarget(logger: Logger, name: String): Targets.Option = {
    Targets.Option.values().find(it => it.toString.equals(name)) match {
      case Some(t) => t
      case _ =>
        logger.error(s"Unable to find target: $name")
        logger.error("List of known targets: ")
        Targets.Option.values() foreach { it => logger.error(it.toString) }
        throw new RuntimeException(s"Unable to find target: $name")
    }
  }

  private lazy val dslDefaultSettings = Seq(
    dslLibraries := Map.empty,
    dslSources := Map.empty,
    dslCompiler := "",
    dslServerMode := false,
    dslServerPort := Some(55662),
    dslPostgres := "",
    dslOracle := "",
    dslApplyMigration := false,
    dslNamespace := "",
    dslSettings := Nil,
    dslDslPath := baseDirectory.value / "dsl",
    dslDependencies := Map.empty,
    dslSqlPath := baseDirectory.value / "sql",
    dslLatest := true,
    dslForce := false,
    dslPlugins := Some(baseDirectory.value)
  )

  private lazy val dslTasks = Seq(
    dslLibrary in Compile <<= dslLibraryTask(Compile),
    dslLibrary in Test <<= dslLibraryTask(Test),
    dslSource <<= dslSourceTask,
    dslResource in Compile <<= dslResourceTask(Compile),
    dslResource in Test <<= dslResourceTask(Test),
    dslMigrate <<= dslMigrateTask,
    dslExecute <<= dslExecuteTask
  )

  private lazy val dslCompilationSettings = inConfig(DslPlatform)(
    Defaults.compileInputsSettings ++
    Defaults.compileAnalysisSettings ++
    Defaults.packageTaskSettings(packageBin, Defaults.packageBinMappings) ++
    Seq(
      sourceDirectories := Seq(),
      unmanagedSources := Seq(),
      managedSources <<= dslSourcesForLibrary,
      sources <<= managedSources,
      manipulateBytecode := compileIncremental.value,
      compileIncremental <<= Defaults.compileIncrementalTask tag (Tags.Compile, Tags.CPU),
      compileIncSetup <<= Defaults.compileIncSetupTask,
      compile <<= Defaults.compileTask,
      classDirectory := crossTarget.value / (configuration.value.name + "-classes"),
      compileAnalysisFilename <<= compileAnalysisFilename in Compile,
      dependencyClasspath <<= Classpaths.concat(managedClasspath in Compile, unmanagedClasspath in Compile),
      copyResources <<= Def.task { Seq() },
      products <<= Classpaths.makeProducts,
      packageOptions <<= Def.task { Seq(
        Package.addSpecManifestAttributes(name.value, version.value, organizationName.value),
        Package.addImplManifestAttributes(name.value, version.value, homepage.value, organization.value, organizationName.value)
      )},
      sbt.Keys.`package` := packageBin.value,
      artifactPath in packageBin <<= artifactPathSetting(artifact in packageBin in DslPlatform)

    )
  )

  override lazy val projectSettings = dslDefaultSettings ++ dslTasks ++ dslCompilationSettings ++ Seq(
    onLoad := {
      if (dslServerMode.value) {
        Actions.setupServerMode(dslCompiler.value, None, dslServerPort.value)
      }
      onLoad.value
    }
  )

  private def artifactPathSetting(art: SettingKey[Artifact]) =
    (dslLibraries, crossTarget, projectID, art, scalaVersion in artifactName, scalaBinaryVersion in artifactName, artifactName) {
      (dslLibs, t, module, a, sv, sbv, toString) => {
        val targetFolder = dslLibs.get(Targets.Option.REVENJ_SCALA).getOrElse(t)
        val dslArtifact = a.copy(name = a.name + "-dsl", classifier = None)
        targetFolder / Artifact.artifactName(ScalaVersion(sv, sbv), module, dslArtifact) asFile
      }
    }

  private def dslLibraryTask(config: Configuration) = Def.inputTask {
    val args = Parsers.spaceDelimited("<arg>").parsed
    def compile(dslTarget: Targets.Option, targetPath: File, targetDeps: Option[File]): Unit = {
      if(dslTarget == Targets.Option.REVENJ_SCALA) {
        (packageBin in DslPlatform).value
      } else {
        Actions.compileLibrary(
          streams.value.log,
          dslTarget,
          targetPath,
          dslDslPath.value,
          dslPlugins.value,
          dslCompiler.value,
          dslServerMode.value,
          dslServerPort.value,
          dslNamespace.value,
          dslSettings.value,
          targetDeps,
          (Classpaths.concat(managedClasspath in Compile, unmanagedClasspath in Compile)).value,
          dslLatest.value)
      }
    }
    if (args.isEmpty) {
      if (dslLibraries.value.isEmpty) throw new RuntimeException(
        """|dslLibraries is empty.
           |Either define dslLibraries in build.sbt or provide target argument (eg. revenj.scala).
           |Usage example: dslLibrary revenj.scala path_to_jar""".stripMargin)

      dslLibraries.value foreach { case (targetArg, targetOutput) =>
        val targetDeps = dslDependencies.value.get(targetArg)
        compile(targetArg, targetOutput, targetDeps)
      }
    } else if (args.length > 2) {
      throw new RuntimeException("Too many arguments. Usage example: dslLibrary revenj.scala path_to_jar")
    } else {
      val targetArg = findTarget(streams.value.log, args.head)
      val predefinedOutput = dslLibraries.value.get(targetArg)
      if (args.length == 1 && predefinedOutput.isEmpty) {
        throw new RuntimeException(
          s"""|dslLibraries does not contain definition for $targetArg.
              |Either define it in dslLibraries or provide explicit output path.
              |Example: dslLibrary revenj.scala path_to_jar""".stripMargin)
      }
      val targetOutput = if (args.length == 2) new File(args.last) else predefinedOutput.get
      val targetDeps = dslDependencies.value.get(targetArg)
      compile(targetArg, targetOutput, targetDeps)
    }
  }

  private def dslSourcesForLibrary = Def.task {
    def generate(dslTarget: Targets.Option, targetPath: File): Seq[File] = {
      Actions.generateSource(
        streams.value.log,
        dslTarget,
        targetPath,
        dslDslPath.value,
        dslPlugins.value,
        dslCompiler.value,
        dslServerMode.value,
        dslServerPort.value,
        dslNamespace.value,
        dslSettings.value,
        dslLatest.value)
    }

    val buffer = new ArrayBuffer[File]()
    if (dslLibraries.value.isEmpty)
      throw new RuntimeException("dslLibraries is empty.")

    dslLibraries.value foreach { case (targetArg, _) =>
      buffer ++= generate(targetArg, target.value / "dsl-temp" / targetArg.name())
    }
    buffer.toSeq
  }

  private def dslSourceTask = Def.inputTask {
    val args = Parsers.spaceDelimited("<arg>").parsed
    def generate(dslTarget: Targets.Option, targetPath: File): Seq[File] = {
      Actions.generateSource(
        streams.value.log,
        dslTarget,
        targetPath,
        dslDslPath.value,
        dslPlugins.value,
        dslCompiler.value,
        dslServerMode.value,
        dslServerPort.value,
        dslNamespace.value,
        dslSettings.value,
        dslLatest.value)
    }
    val buffer = new ArrayBuffer[File]()
    if (args.isEmpty) {
      if (dslSources.value.isEmpty) throw new RuntimeException(
        """|dslSources is empty.
           |Either define dslSources in build.sbt or provide target argument (eg. revenj.scala).
           |Usage example: dslSource revenj.scala path_to_folder""".stripMargin)
      dslSources.value foreach { case (targetArg, targetOutput) =>
        buffer ++= generate(targetArg, targetOutput)
      }
    } else if (args.length > 2) {
      throw new RuntimeException("Too many arguments. Usage example: dslSource revenj.scala path_to_target_source_folder")
    } else {
      val targetArg = findTarget(streams.value.log, args.head)
      val predefinedOutput = dslSources.value.get(targetArg)
      if (args.length == 1 && predefinedOutput.isEmpty) {
        throw new RuntimeException(
          s"""|dslSources does not contain definition for $targetArg.
              |Either define it in dslSources or provide explicit output path.
              |Example: dslLibrary revenj.scala path_to_folder""".stripMargin)
      }
      val targetOutput = if (args.length == 2) new File(args.last) else predefinedOutput.get
      buffer ++= generate(targetArg, targetOutput)
    }
    buffer.toSeq
  }

  private def dslResourceTask(config: Configuration) = Def.inputTask {
    val args = Parsers.spaceDelimited("<arg>").parsed
    def generate(dslTarget: Targets.Option, targetPath: Option[File]): Seq[File] = {
      Actions.generateResources(
        streams.value.log,
        dslTarget,
        targetPath.getOrElse((resourceDirectory in Compile).value / "META-INF" / "services"),
        Seq((target in config).value),
        (dependencyClasspath in config).value)
    }
    val buffer = new ArrayBuffer[File]()
    if (args.isEmpty) {
      if (dslSources.value.isEmpty && dslLibraries.value.isEmpty) throw new RuntimeException(
        """|Both dslSources and dslLibraries is empty.
           |Either define dslSources/dslLibraries in build.sbt or provide target argument (eg. revenj.scala).
           |Usage example: dslResource revenj.scala""".stripMargin)
      (dslSources.value.keys ++ dslLibraries.value.keys).toSet[Targets.Option] foreach { target =>
        buffer ++= generate(target, None)
      }
    } else if (args.length > 2) {
      throw new RuntimeException("Too many arguments. Usage example: dslSource revenj.scala path_to_meta_inf_services_folder")
    } else {
      val targetArg = findTarget(streams.value.log, args.head)
      if (args.length == 1) {
        throw new RuntimeException(
          """|dslSources does not contain definition for $targetArg.
             |Either define it in dslSources or provide explicit output path.
             |Example: dslLibrary revenj.scala path_to_folder""".stripMargin)
      }
      val targetOutput = if (args.length == 2) new File(args.last) else (resourceDirectory in config).value / "META-INF" / "services"
      buffer ++= generate(targetArg, Some(targetOutput))
    }
    buffer.toSeq
  }

  private def dslMigrateTask = Def.inputTask {
    def migrate(pg: Boolean, jdbc: String): Unit = {
      Actions.dbMigration(
        streams.value.log,
        jdbc,
        pg,
        dslSqlPath.value,
        dslDslPath.value,
        dslPlugins.value,
        dslCompiler.value,
        dslServerMode.value,
        dslServerPort.value,
        dslApplyMigration.value,
        dslForce.value,
        dslLatest.value)
    }
    if (dslPostgres.value.nonEmpty) {
      migrate(pg = true, dslPostgres.value)
    }
    if (dslOracle.value.nonEmpty) {
      migrate(pg = false, dslOracle.value)
    } else if (dslPostgres.value.isEmpty) {
      streams.value.log.error("Jdbc connection string not defined for Postgres or Oracle")
    }
  }

  private def dslExecuteTask = Def.inputTask {
    val args = Parsers.spaceDelimited("<arg>").parsed
    Actions.execute(
      streams.value.log,
      dslDslPath.value,
      dslPlugins.value,
      dslCompiler.value,
      dslServerMode.value,
      dslServerPort.value,
      args)
  }
}
