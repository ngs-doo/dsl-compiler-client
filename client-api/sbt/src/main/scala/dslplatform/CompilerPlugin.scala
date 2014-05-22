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
    lazy val projectIniPath = settingKey[Option[File]]("Location of project definition.")

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
    lazy val dslSources = taskKey[PathFinder]("Filter for .dsl Sources, by default this are all .dsl files in the classpath!")
    lazy val dslFiles = taskKey[Map[String, String]]("Map of dsls and their names.")

    lazy val databaseConnection = settingKey[Map[String, String]]("In flux, Properties for connection to database.")

    lazy val generateSources = taskKey[List[Source]]("Generate sources from given dsl!")
    lazy val generateSourcesUnmanaged = taskKey[Unit]("Generate sources from given dsl!")
    lazy val updateDatabase = taskKey[List[Source]]("Upgrade database with given dsl!")
    lazy val parseDSL = taskKey[Boolean]("Parse Dsl Sources!")

    lazy val upgradeScalaServer = taskKey[Unit]("In flux! Task that enables applying upgrade to unmanaged Scala server.")
    lazy val upgradeCSharpServer = taskKey[Unit]("In flux! Task that enables applying upgrade to unmanaged Mono server.")

    lazy val monoTempFolder = settingKey[File]("In flux! Place where to temporarily store csharp files. ")
    lazy val monoDependencyFolder = settingKey[File]("In flux! Where dependencies for compilation of cs files are located.")

    lazy val monoServerLocation = settingKey[File]("In flux! Location where mono application resides, upgradeMonoServer task will copy all files here to /bin folder")
  }

  import collection.JavaConversions._

  lazy val dslSettings = Seq(
    api := new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), new UnmanagedDSLImpl()),
    projectIniPath := None,
    projectConfiguration <<= projectConfigurationDef(),
    dslCharset := Charsets.UTF_8,
    databaseConnection := Map(),
    testProject := false,
    outputDirectory := None,
    outputPathMapping <<= OutputPathMapping.plainMapping,
    migration := "unsafe",
    sourceOptions := Set("with-active-record"),
    targetSources := Set("Scala"),
    dslDirectory := baseDirectory.value / "dsl" ** "*.dsl",
    dslFiles <<= dslFilesDef,

    username        := projectConfiguration.value.get("username").getOrElse(""),
    password        := projectConfiguration.value.get("password").getOrElse(""),
    dslProjectId    := projectConfiguration.value.get("project-id").getOrElse(""),
    packageName     := projectConfiguration.value.get("package-name").getOrElse("model"),
    token           := com.dslplatform.compiler.client.api.config.Tokenizer.tokenHeader(username.value, password.value),

    monoTempFolder            := file("mono_src_tmp"),
    monoDependencyFolder      := file("revenj_lib"),
    monoServerLocation        := file("mono"),

    parseDSL                  <<= parseDSLDef(),
    generateSources           <<= generateAndReturnSourcesDef(),
    generateSourcesUnmanaged  <<= generateSourcesDef(),
    updateDatabase            <<= upgradeManagedDatabaseAndReturnSourceDef,
    upgradeScalaServer        <<= unmanagedUpgradeDef("ScalaServer"),
    upgradeCSharpServer         <<= unmanagedUpgradeDef("CSharpServer")
  )

  private def dslFilesDef(): Def.Initialize[Task[Map[String, String]]] = Def.task {
    (dslDirectory in Compile).value.get.map {
      file => file.name -> IO.read(file, Charsets.UTF_8)
    }.toMap
  }

  private def parseDSLDef(): Def.Initialize[Task[Boolean]] = Def.task {
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
      log.error("Source generation Unsuccessful!")
      log.error(Option(response.authorizationErrorMessage).getOrElse("Missing error message."))
    }
    response.sources.toList
  }

  private def generateSourcesDef(): Def.Initialize[Task[Unit]] = Def.task {
    //writeSources(generateSources.value, outputPathMapping.value, streams.value.log)
  }

  private def upgradeManagedDatabaseAndReturnSourceDef(): Def.Initialize[Task[List[Source]]] = Def.task {
    val log = streams.value.log
    val response = api.value.updateManagedProject(token.value, java.util.UUID.fromString(dslProjectId.value), targetSources.value, packageName.value, migration.value, sourceOptions.value, dslFiles.value)
    if (!response.authorized) log.error(response.authorizationErrorMessage)
    response.sources.toList
  }

  private def unmanagedUpgradeAndReturnSourceDef(serverLanguage: String): Def.Initialize[Task[List[Source]]] = Def.taskDyn {
    val log = streams.value.log
    val targets: java.util.Set[String] = targetSources.value + serverLanguage
    val dsl = dslFilesDef.value
    Def.task {
      val migration = generateMigrationSQLDef(dsl).value
      val sources = api.value.generateUnmanagedSources(token.value, packageName.value, targets, sourceOptions.value, dsl)
      if (sources.authorized) log.info("Request for migration sources successful.") else sys.error("Update failed: unauthorized. " + sources.authorizationErrorMessage)
      val upgradeResult = api.value.upgradeUnmanagedDatabase(dataSource.value, List(migration))
      if (upgradeResult.successfulUpgrade) log.info("Database upgrade successful.") else sys.error("Migration failed!" + upgradeResult.databaseConnectionErrorMessage)
      sources.sources.toList
    }
  }

  private def unmanagedUpgradeDef(serverLanguage: String): Def.Initialize[Task[Unit]] = Def.taskDyn {
    streams.value.log.debug(s"Calling server upgrade for $serverLanguage")
    writeSourcesDef(unmanagedUpgradeAndReturnSourceDef(serverLanguage), csOutputMapping).value
    Def.task {
      monoCompileAndDeployDef.value
    }
  }

  private def generateMigrationSQLDef(dslFiles: Map[String, String]): Def.Initialize[Task[String]] = Def.task {

    val migration = api.value.generateMigrationSQL(token.value, dataSource.value, dslFiles)
    if (!migration.migrationRequestSuccessful)
      sys.error(s"Migration source request failed: ${migration.authorizationErrorMessage}" )
    // TODO : may be an outdated DB, ask for a blind migration.

    streams.value.log.debug(s"Migration to apply: ${migration.migration}")
    // TODO : ask for confirmation.
    migration.migration
  }

  private def dataSource = Def.setting {
    val dbc = databaseConnection.value
    new org.postgresql.ds.PGSimpleDataSource() {
      setServerName(dbc("ServerName"))
      setPortNumber(dbc("Port").toInt)
      setDatabaseName(dbc("DatabaseName"))
      setUser(dbc("User"))
      setPassword(dbc("Password"))
      setSsl(false)
    }
  }

  private def projectConfigurationDef(): Def.Initialize[Map[String, String]] = Def.setting {
    projectIniPath.value.fold(Map.empty[String, String]) {
      projectIniPath =>
        val properties = new java.util.Properties()
        properties.load(new java.io.FileInputStream(projectIniPath))
        Map(
          "project-id" -> properties.getProperty("project-id", ""),
          "username" -> properties.getProperty("username", ""),
          "api-url" -> properties.getProperty("api-url", ""),
          "package-name" -> properties.getProperty("package-name", "")
        ).filter((kv) => kv._2 != "")
    }
  }

  private def writeSourcesDef(
      sourceGenerator: Def.Initialize[Task[List[Source]]],
      outputPathMapping: Def.Initialize[OutputPathMappingType]): Def.Initialize[Task[Int]] = Def.task {
    val log = streams.value.log
    log.info("About to preform file write.")
    val sources = sourceGenerator.value
    val writeCount = sources.map(outputPathMapping.value).map {
      case (path: File, content: Array[Byte]) =>
        IO.write(path, content)
        log.info(s"Wrote ${path.getAbsolutePath}")
    }.size
    log.info(s"Writing complete. Wrote $writeCount files")
    0
  }

  object OutputPathMapping {
    type OutputPathMappingType = PartialFunction[Source, (File, Array[Byte])]

    def interface_service_mapping(iDir: String, sDir: String): Def.Initialize[OutputPathMappingType] = Def.setting {
      case Src(language, path, content) =>
        (file(if (path.contains("postgres")) sDir else iDir) / language.toString / path, content)
    }

    def plainMapping: Def.Initialize[OutputPathMappingType] = Def.setting {
      case Src(language, path, content) if outputDirectory.value.nonEmpty =>
        (outputDirectory.value.get / language / path, content)
    }

    val csOutputMapping: Def.Initialize[OutputPathMappingType] = Def.setting {
      val csParc:  OutputPathMappingType = {
        case Src(language, path, content) if (language.toLowerCase == "csharpserver") =>
          (monoTempFolder.value / path, content)
      }
      csParc orElse outputPathMapping.value
    }
  }

  object Src {
    def unapply(s: com.dslplatform.compiler.client.response.Source) = Some(s.language, s.path, s.content)
  }

  private def monoCompileAndDeployDef(): Def.Initialize[Task[Unit]] = Def.task {
    val log = streams.value.log
    log.info("About to Compile CS files and deploy them to server location.")

    import scala.sys.process._
    val systemDeps = Seq("System.ComponentModel.Composition", "System", "System.Data", "System.Xml", "System.Runtime.Serialization", "System.Configuration", "System.Drawing")
    val monoLib: File = monoDependencyFolder.value
    val revenj = monoLib.listFiles.map(_.getName).filter(_.endsWith("dll"))

    val deps = (systemDeps ++ revenj).map(d => s"-r:$d")

    val mono_app: File = monoServerLocation.value
    val assembly_name = s"${mono_app}/bin/generatedModel.dll"
    if (!(mono_app / "bin").exists()) {
      (mono_app / "bin" ).mkdirs()
      (mono_app / "bin" ).mkdir()
    }

    val cmd = Seq("mcs", "-v", s"-out:$assembly_name", "-target:library", s"-lib:$monoLib") ++ deps :+ s"-recurse:${monoTempFolder.value}/*.cs"

    IO.write(file("runScript.sh"), cmd.mkString(" "), Charsets.UTF_8)
    log.info(Seq("sh", "runScript.sh").!!)
    log.info(s"chgrp mono $assembly_name " !!)
    monoLib.listFiles.foreach{
      file =>
        val cmd = s"install -g mono -m 750 ${file.getAbsolutePath} $mono_app/bin/"
        cmd.split(" ").toSeq.!!
    }
  }
}
