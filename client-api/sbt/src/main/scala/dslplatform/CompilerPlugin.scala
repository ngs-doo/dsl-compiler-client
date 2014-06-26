package dslplatform

import sbt._
import sbt.Keys._

import com.dslplatform.compiler.client.{HttpTransportProvider, ApiImpl, Api}
import com.dslplatform.compiler.client.api.core.impl._
import com.dslplatform.compiler.client.response.Source
import org.apache.commons.codec.Charsets

object CompilerPlugin extends sbt.Plugin {

  import DslKeys._
  import OutputPathMapping._

  override lazy val projectSettings: Seq[Setting[_]] = dslSettings

  object DslKeys {
    lazy val testProject = settingKey[Boolean]("Is the project test, this will automatically upgrade the database every time you change dsl.")
    lazy val projectPropsPath = settingKey[Option[File]]("Location of project definition.")

    lazy val dslCharset = settingKey[java.nio.charset.Charset]("Charset for dsl files")
    lazy val projectConfiguration = settingKey[Map[String, String]]("Project specific properties.")
    lazy val api = settingKey[Api]("Api implementation for this plugin.")
    lazy val username = settingKey[String]("User name")
    lazy val password = settingKey[String]("Password")
    lazy val dslProjectId = settingKey[String]("projectId")
    lazy val packageName = settingKey[String]("package name")

    lazy val migration = settingKey[String]("Is migration approved.")
    lazy val outputDirectory = settingKey[Option[File]]("Output directory!")
    lazy val outputPathMapping = settingKey[OutputPathMappingType]("Maps a targetLanguage and an output source name to the output path, by default this is set as outputDirectory / targetSources / source name.")
    lazy val sourceOptions = settingKey[Set[String]]("Options to generated Source.")
    lazy val targetSources = settingKey[Set[String]]("Languages to generated Source.")

    lazy val token = taskKey[String]("Token used to authenticate your remote request.")
    lazy val dslDirectory = settingKey[PathFinder]("Location of .dsl Sources, dsl!")
    lazy val dslFiles = taskKey[Map[String, String]]("Map of dsls and their names.")

    lazy val databaseConnection = settingKey[Map[String, String]]("In flux, Properties for connection to database.")

    lazy val getDiffString = taskKey[String]("Generate diff.")
    lazy val getDiff = taskKey[Unit]("Prints diff to console.")
    lazy val generateSourcesList = taskKey[List[Source]]("Generate sources from a given dsl!")
    lazy val generateSources = taskKey[Unit]("Generate sources from a given dsl!")
    lazy val upgradeDatabaseList = taskKey[List[Source]]("Upgrade database with a given dsl!")
    lazy val upgradeDatabase = taskKey[Unit]("Upgrade database with a given dsl!")
    lazy val parseDSL = taskKey[Boolean]("Parse Dsl Sources!")

    lazy val generateUnmanagedCSSources = taskKey[Unit]("In flux! Task that enables applying upgrade to unmanaged Mono server.")
    lazy val compileCSharpServer = taskKey[Int]("In flux! Task that compiles C# source into generatedModel.dll needed to run Mono server.")
    lazy val upgradeScalaServer = taskKey[Unit]("In flux! Task that enables applying upgrade to unmanaged Scala server.")
    lazy val upgradeCSharpServer = taskKey[Unit]("In flux! Task that enables applying upgrade to unmanaged Mono server.")

    lazy val monoTempFolder = settingKey[File]("In flux! Place where to temporarily store C# files. ")
    lazy val monoDependencyFolder = settingKey[File]("In flux! Where dependencies for compilation of cs files are located.")

    lazy val monoServerLocation = settingKey[File]("In flux! Location where mono application resides, upgradeMonoServer task will copy all files here to /bin folder")
    lazy val generatedModel = settingKey[File]("")

    // °º¤ø,¸¸,ø¤º°`°º¤ø, Build Workout ,ø¤°º¤ø,¸¸,ø¤º°`°º¤ø,¸
    lazy val upgradeUnmanagedDatabase = taskKey[Unit]("Upgrades the database or writes migration SQL to a file depending on other settings")
    lazy val migrationOutputFile = settingKey[Option[File]]("If defined will write migration to it")
    lazy val performDatabaseMigration = settingKey[Boolean]("Should the database be upgraded automatically.")
    lazy val performSourceCompile = settingKey[Boolean]("Should generatedModel.dll be compiled.")
    lazy val performServerDeploy = settingKey[Boolean]("Should the server be deployed automatically.")
  }

  import collection.JavaConversions._

  lazy val dslSettings = Seq(
    api := new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), new UnmanagedDSLImpl()),
    projectPropsPath := None,
    projectConfiguration <<= projectConfigurationDef(),
    dslCharset := Charsets.UTF_8,
    databaseConnection := projectConfiguration.value, // TODO - filter some
    testProject := false,
    outputDirectory := None,
    outputPathMapping <<= OutputPathMapping.plainMapping,
    migration := "unsafe",
    migrationOutputFile := Some(file("migration.sql")),
    sourceOptions := Set("active-record"),
    targetSources := Set("Scala"),
    dslDirectory := baseDirectory.value / "dsl" ** "*.dsl",
    dslFiles <<= dslFilesDef,

    username                  := projectConfiguration.value.getOrElse("username", ""),
    password                  := projectConfiguration.value.getOrElse("password", ""),
    dslProjectId              := projectConfiguration.value.getOrElse("project-id", ""),
    packageName               := projectConfiguration.value.getOrElse("dsl.package-name", "model"),
    token                     := com.dslplatform.compiler.client.api.config.Tokenizer.basicHeader(username.value, password.value),

    monoTempFolder            := file("mono_src_tmp"),
    monoDependencyFolder      := file("revenj_lib"),
    monoServerLocation        := file("mono"),
    generatedModel            := monoServerLocation.value / "bin" / "generatedModel.dll",

    getDiffString             <<= getDiffStringDef,
    getDiff                   <<= getDiffDef,
    parseDSL                  <<= parseDSLDef,
    generateSourcesList       <<= generateAndReturnSourcesDef(),
    generateSources           <<= generateSourcesDef(),
    upgradeDatabaseList       <<= upgradeManagedDatabaseAndReturnSourceDef,
    upgradeDatabase           <<= upgradeManagedDatabaseDef,

    generateUnmanagedCSSources <<= generateUnmanagedSourcesDef("CSharpServer"),
    upgradeUnmanagedDatabase  <<= unmanagedDatabaseUpgradeDef,
    upgradeScalaServer        <<= unmanagedServerUpgradeDef("ScalaServer").andFinally(upgradeUnmanagedDatabase),
    compileCSharpServer       <<= monoCompileDef,
    upgradeCSharpServer       <<= unmanagedServerUpgradeDef("CSharpServer").andFinally(upgradeUnmanagedDatabase),

    migrationOutputFile       := Some(file("migration.sql")),
    performDatabaseMigration  := false,
    performServerDeploy       := false
  )

  private def dslFilesDef: Def.Initialize[Task[Map[String, String]]] = Def.task {
    (dslDirectory in Compile).value.get.map {
      file => file.name -> IO.read(file, Charsets.UTF_8)
    }.toMap
  }

  private def getLastDsl: Def.Initialize[Task[Map[String, String]]] = Def.task {
    if (testProject.value) {
      val pid = java.util.UUID.fromString(dslProjectId.value)
      api.value.getLastManagedDSL(token.value, pid).dsls.toMap
    } else {
      dataSource.value.flatMap { ds =>
        val getLastDslResponse = api.value.getLastUnmanagedDSL(ds)
        if (!getLastDslResponse.databaseConnectionSuccessful)
          sys.error(s"Database connection fail: ${getLastDslResponse.databaseConnectionErrorMessage}")
        Option(getLastDslResponse.lastMigration).map(_.dsls.toMap)
      }.getOrElse(Map.empty[String, String])
    }
  }

  private def getDiffStringDef: Def.Initialize[Task[String]] = Def.task {
    api.value.getDiff(getLastDsl.value, dslFiles.value)
  }

  private def getDiffDef: Def.Initialize[Task[Unit]] = Def.task {
    val diff = getDiffStringDef.value
    streams.value.log.info("diff:/n" + diff)
  }

  private def parseDSLDef: Def.Initialize[Task[Boolean]] = Def.task {
    val log = streams.value.log

    val response = api.value.parseDSL(token.value, dslFiles.value)
    log.info("Parse: " + response.parseMessage)
    response.parsed
  }

  private def generateAndReturnSourcesDef(): Def.Initialize[Task[List[Source]]] = Def.task {
    val log = streams.value.log
    val response = api.value.generateSources(token.value, java.util.UUID.fromString(dslProjectId.value), targetSources.value, packageName.value, sourceOptions.value)
    if (!response.authorized) log.error(response.authorizationErrorMessage)
    if (response.generatedSuccess) log.info("Source generation successful.")
    else {
      log.error("Source generation unsuccessful!")
      log.error(Option(response.authorizationErrorMessage).getOrElse("Missing error message."))
    }
    response.sources.toList
  }

  private def generateSourcesDef(): Def.Initialize[Task[Unit]] = Def.taskDyn {
    val sources = generateSourcesList.value
    val outputPathMapping = OutputPathMapping.plainMapping.value
    Def.task {
      writeSourcesDef(sources.toList, outputPathMapping, streams.value.log)
    }
  }

  private def upgradeManagedDatabaseDef(): Def.Initialize[Task[Unit]] = Def.taskDyn{
    val source = upgradeManagedDatabaseAndReturnSourceDef().value
    Def.task {
      writeSourcesDef(source, OutputPathMapping.plainMapping.value, streams.value.log)
    }
  }

  private def upgradeManagedDatabaseAndReturnSourceDef(): Def.Initialize[Task[List[Source]]] = Def.task {
    val log = streams.value.log
    val response = api.value.updateManagedProject(token.value, java.util.UUID.fromString(dslProjectId.value), targetSources.value, packageName.value, migration.value, sourceOptions.value, dslFiles.value)
    if (!response.authorized) log.error(response.authorizationErrorMessage)
    response.sources.toList
  }

  private def generateUnmanagedSourcesDef(serverLanguage: String): Def.Initialize[Task[Unit]] = Def.task {
    val log = streams.value.log
    val dsl = dslFilesDef.value
    val targets: java.util.Set[String] = targetSources.value + serverLanguage
    val generateUnmanagedSources = api.value.generateUnmanagedSources(token.value, packageName.value, targets, sourceOptions.value, dsl)
    if (generateUnmanagedSources.authorized) log.info("Request for migration sources successful.") else sys.error("Update failed: unauthorized. " + generateUnmanagedSources.authorizationErrorMessage)
    log.info(s"Generated ${generateUnmanagedSources.sources.size()} sources!")
    val mapping = csOutputMapping.value // todo - case around ScalaServer
    writeSourcesDef(generateUnmanagedSources.sources.toList, mapping, log)
  }

  private def unmanagedServerUpgradeDef(serverLanguage: String): Def.Initialize[Task[Unit]] = Def.taskDyn {
    val log = streams.value.log
    log.debug(s"Calling server upgrade for $serverLanguage")
    generateUnmanagedSourcesDef(serverLanguage).value
    Def.task {
      serverLanguage match {
        case "CSharpServer" =>
          if (monoCompileDef.value == 0) {
            log.info("Compilation successful")
            if (performServerDeploy.value)
              monoDeploy(monoDependencyFolder.value, monoServerLocation.value, monoTempFolder.value, streams.value.log)
          }
        case _ => ()
      }
    }
  }

  private def unmanagedDatabaseUpgradeDef: Def.Initialize[Task[Unit]] = Def.taskDyn {
    val log = streams.value.log
    val migration = generateMigrationSQLDef().value
    Def.task {
      if (performDatabaseMigration.value) {
        // TODO : ask for confirmation.
        val upgradeResult = api.value.upgradeUnmanagedDatabase(dataSource.value.getOrElse(null), List(migration))
        if (upgradeResult.successfulUpgrade) log.info("Database upgrade successful.") else sys.error("Migration failed! " + upgradeResult.databaseConnectionErrorMessage)
      }
      migrationOutputFile.value.map{ migrationOutPath => IO.write(migrationOutPath, migration, Charsets.UTF_8)}
    }
  }

  private def generateMigrationSQLDef(): Def.Initialize[Task[String]] = Def.task {
    val migration = api.value.generateMigrationSQL(token.value, dataSource.value.getOrElse(null), dslFiles.value)
    if (!migration.migrationRequestSuccessful)
      sys.error(s"Migration SQL request failed: ${migration.authorizationErrorMessage}" )
    // TODO : may be an outdated DB, migrate with old dsl then with new.

    streams.value.log.debug(s"Migration to apply: ${migration.migration}")
    migration.migration
  }

  private def dataSource = Def.setting {
    val dbc = databaseConnection.value
    scala.util.Try(
      Some(new org.postgresql.ds.PGSimpleDataSource() {
        setServerName(dbc("ServerName"))
        setPortNumber(dbc("Port").toInt)
        setDatabaseName(dbc("DatabaseName"))
        setUser(dbc("User"))
        setPassword(dbc("Password"))
        setSsl(false)
      })
    ).getOrElse(None)
  }

  private def projectConfigurationDef(): Def.Initialize[Map[String, String]] = Def.setting {
    projectPropsPath.value.fold(Map.empty[String, String]) {
      projectPropsPath =>
        val properties = com.typesafe.config.ConfigFactory.parseFile(projectPropsPath)
        def getConfig(key: String) = if (properties.hasPath(key)) Some(properties.getString(key)) else None
        // TODO - do something
        Map(
          "project-id"    -> getConfig("dsl.projectId"),
          "username"      -> getConfig("dsl.username"),
          "password"      -> getConfig("dsl.password"),
          "package-name"  -> getConfig("dsl.package-name"),
          "ServerName"    -> getConfig("db.ServerName"),
          "Port"          -> getConfig("db.Port"),
          "DatabaseName"  -> getConfig("db.DatabaseName"),
          "User"          -> getConfig("db.User"),
          "Password"      -> getConfig("db.Password")
        ).collect{case (k: String, Some(v: String)) => k -> v}
    }
  }

  private def writeSourcesDef(
      source: List[Source],
      outputPathMapping: OutputPathMappingType,
     log: sbt.Logger): Int = {
    log.info("About to preform file write.")
    val writeCount = source.map(outputPathMapping).map {
      case (path: File, content: Array[Byte]) =>
        IO.write(path, content)
        log.info(s"Wrote ${path.getAbsolutePath}")
    }.size
    log.info(s"Writing complete. Wrote $writeCount files")
    0
  }

  private def monoCompileDef: Def.Initialize[Task[Int]] = Def.task {
    val monoLib = monoDependencyFolder.value
    val log = streams.value.log

    log.info("About to Compile CS files and deploy them to server location.")
    import scala.sys.process._
    if (!monoLib.canRead) {
      log.error(s"Revenj library is not present at ${monoLib.getAbsolutePath}")
      1
    }
    else {
      val mono_app = monoServerLocation.value
      val monoTmp = monoTempFolder.value
      val systemDeps = Seq("System.ComponentModel.Composition", "System", "System.Data", "System.Xml", "System.Runtime.Serialization", "System.Configuration", "System.Drawing")
      val revenj = monoLib.listFiles.map(_.getName).filter(_.endsWith("dll"))
      val deps = (systemDeps ++ revenj).map(d => s"-r:$d")
      val assemblyNameVal = generatedModel.value

      if (!(mono_app / "bin").exists()) {
        (mono_app / "bin").mkdirs()
        (mono_app / "bin").mkdir()
      }
      val cmd = Seq("mcs", "-v", s"-out:$assemblyNameVal", "-target:library", s"-lib:$monoLib") ++ deps :+ s"-recurse:${monoTmp}/*.cs"

      // Make a mono compile command.

      IO.write(file("runScript.sh"), cmd.mkString(" "), Charsets.UTF_8)
      log.info(Seq("sh", "runScript.sh").!!)

      log.info(s"Successfully compiled mono at ${mono_app.getAbsoluteFile}")
      0
    }
  }

  private def monoDeploy(monoLib: File, mono_app: File, monoTempFolder: File, log: sbt.Logger): Int = {
    log.info("About to Compile CS files and deploy them to server location.")
    import scala.sys.process._
    if (!(mono_app / "bin").exists()) {
      (mono_app / "bin" ).mkdirs()
      (mono_app / "bin" ).mkdir()
    }

    // Make a start script.
    val startScript =
      s"""#!/bin/sh
        |cd "$$(dirname "$$0")"/bin
        |exec mono Revenj.Http.exe "$$@" > ../logs/mono.log 2>&1
        |""".stripMargin

    val startScriptFile = file(s"$mono_app" + "/start.sh")
    log.info(s"Wrote start script to ${startScriptFile.getAbsoluteFile}")
    IO.write(startScriptFile, startScript, Charsets.UTF_8)
    Seq("chmod", "700", startScriptFile.getAbsolutePath).!!

    // Copy revenj dependencies.
    monoLib.listFiles.foreach{
      file =>
        val cmd = s"cp ${file.getAbsolutePath} $mono_app/bin/"
        cmd.split(" ").toSeq.!!
    }
    log.info(s"Successfully deployed mono at ${mono_app.getAbsoluteFile}")
    0
  }

  object OutputPathMapping {
    type OutputPathMappingType = PartialFunction[Source, (File, Array[Byte])]

    def interface_service_mapping(iDir: String, sDir: String): Def.Initialize[OutputPathMappingType] = Def.setting {
      case Src(language, path, content) =>
        (file(if (path.contains("postgres")) sDir else iDir) / language.toString / path, content)
    }

    def plainMapping: Def.Initialize[OutputPathMappingType] = Def.setting {
      Function.unlift {
        case Src(language, path, content) =>
          outputDirectory.value.map{ b => (b / language / path, content)}
        case _ => None
      }
    }

    val csOutputMapping: Def.Initialize[OutputPathMappingType] = Def.setting {
      val csParc:  OutputPathMappingType = {
        case Src(language, path, content) if language.toLowerCase == "csharpserver" =>
          (monoTempFolder.value / path, content)
      }
      csParc orElse outputPathMapping.value
    }
  }

  object Src {
    def unapply(s: com.dslplatform.compiler.client.response.Source): Option[(String, String, Array[Byte])] =  Some(s.language, s.path, s.content)
  }
}