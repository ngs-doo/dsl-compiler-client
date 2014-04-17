package dslplatform

import sbt._
import sbt.Keys._

import com.dslplatform.compiler.client.{HttpTransportProvider, ApiImpl, Api}
import com.dslplatform.compiler.client.api.core.impl._
import org.apache.commons.codec.Charsets

object CompilerPlugin extends sbt.Plugin {
  import DslKeys._
  lazy val dslSettings: Seq[Setting[_]] = baseDslSetting

  object DslKeys {
    lazy val testProject                      = settingKey[Boolean]("Is the project test, this will automatically upgrade the database every time you change dsl.")
    lazy val projectIniPath                   = settingKey[Option[File]]("Location of project definition.")

    lazy val dslCharset                       = settingKey[java.nio.charset.Charset]("Charset for dsl files")
    lazy val projectConfiguration             = settingKey[Map[String, String]]("Project specific properties.")
    lazy val apiImpl                          = settingKey[Api]("Api implementation for this plugin.")
    lazy val username                         = settingKey[String]("User name")
    lazy val password                         = settingKey[String]("Password")
    lazy val dslProjectId                     = settingKey[String]("projectId")
    lazy val packageName                      = settingKey[String]("package name")

    lazy val migration                        = settingKey[String]("Is migration approved.")
    lazy val outputDirectory                  = settingKey[Option[File]]("Output directory!")
    lazy val interfacesOutputDirectory        = settingKey[Option[File]]("(in Flux)Output directory for interface sources (used only in upgradeUnmanagedServer)!")
    lazy val servicesOutputDirectory          = settingKey[Option[File]]("(in Flux)Output directory for interface sources (used only in upgradeUnmanagedServer)!")
    lazy val sourceOptions                    = settingKey[Set[String]]("Options to generated Source.")
    lazy val targetLanguages                  = settingKey[Set[String]]("Languages to generated Source.")

    lazy val token                            = taskKey[String]("Token used to authenticate your remote request.")
    lazy val dslDirectory                     = settingKey[PathFinder]("Location of .dsl Sources, dsl!")
    lazy val dslSources                       = taskKey[PathFinder]("Filter for .dsl Sources, by default this are all .dsl files in the classpath!")
    lazy val dslFiles                         = taskKey[Map[String, String]]("Map of dsls and their names.")

    lazy val databaseConnection               = settingKey[Map[String, String]]("In flux, Properties for connection to database.")
    lazy val generateSources                  = taskKey[Map[String, Map[String, String]]]("Generate sources from given dsl!")
    lazy val generateSourcesUnmanaged         = taskKey[Unit]("Generate sources from given dsl!")
    lazy val generateJavaSources              = taskKey[Seq[File]]("Generate Java sources from given dsl!")
    lazy val generateScalaSources             = taskKey[Seq[File]]("Generate Scala sources from given dsl!")
    lazy val updateDatabase                   = taskKey[Map[String, Map[String, String]]]("Upgrade database with given dsl!")
    lazy val parseDSL                         = taskKey[Boolean]("Parse Dsl Sources!")
    lazy val upgradeScalaServer               = taskKey[Map[String, Map[String, String]]]("Task that enables applying upgrade to unmanaged scala server.")
    lazy val upgradeScalaServerUnmanaged      = taskKey[Unit]("In flux! Task that enables applying upgrade to unmanaged scala server.")
  }

  import collection.JavaConversions._

  lazy val baseDslSetting = Seq(
      apiImpl := new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), new UnmanagedDSLImpl()),
      projectIniPath := None,
      projectConfiguration := {
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
      },
      dslCharset := Charsets.UTF_8,
      databaseConnection := Map(),
      testProject := false,
      outputDirectory := None,
      interfacesOutputDirectory := None,
      servicesOutputDirectory := None,
      migration := "unsafe",
      sourceOptions := Set("with-active-record"),
      targetLanguages := Set("Scala"),
      dslDirectory := baseDirectory.value / "dsl" ** "*.dsl",
      dslFiles := (dslDirectory in Compile).value.get.map {
        file => file.name -> IO.read(file, Charsets.UTF_8)
      }.toMap,
      username := projectConfiguration.value.get("username").getOrElse(""),
      password := projectConfiguration.value.get("password").getOrElse(""),
      dslProjectId := projectConfiguration.value.get("project-id").getOrElse(""),
      packageName := projectConfiguration.value.get("package-name").getOrElse("model"),
      token := com.dslplatform.compiler.client.api.config.Tokenizer.tokenHeader(username.value, password.value, dslProjectId.value),
      parseDSL := {
        val log = streams.value.log
        val response = apiImpl.value.parseDSL(token.value, dslFiles.value)
        log.info("Parse: " + response.parseMessage)
        response.parsed
      },
      generateSources := {
        val log = streams.value.log
        val response = apiImpl.value.generateSources(token.value, java.util.UUID.fromString(dslProjectId.value), targetLanguages.value, packageName.value, sourceOptions.value)
        if (!response.authorized) log.error(response.authorizationErrorMessage)
        if (response.generatedSuccess) log.info("Generate Successful")
        else log.error("Source generation Unsuccessful")
        mapAsScalaMap(response.sources.map {
          source => (source._1 -> source._2.toMap)
        }).toMap
      },
      generateSourcesUnmanaged := {
        val log = streams.value.log
        log.info("Writing to: " + outputDirectory.value.get.getCanonicalPath)
        outputDirectory.value.fold(
          log.error("Need to set output directory to write files to.")
        ) {
          outputDirectory =>
            log.info("Generated sources.")
            generateSources.value.foreach {
              sourceTarget: (String, Map[String, String]) =>
                val language: String = sourceTarget._1
                sourceTarget._2.foreach {
                  case (sourceName, sourceContent) =>
                    val sourceFile: File = (outputDirectory / language) / sourceName
                    log.info("Writing " + sourceFile.getAbsolutePath)
                    IO.write(sourceFile, sourceContent, dslCharset.value, false)
                }
            }
        }
      },
      updateDatabase := {
        val log = streams.value.log
        val response = apiImpl.value.updateManagedProject(token.value, java.util.UUID.fromString(dslProjectId.value), targetLanguages.value, packageName.value, migration.value, sourceOptions.value, dslFiles.value)
        if (!response.authorized) log.error(response.authorizationErrorMessage)
        mapAsScalaMap(response.sources map {
          source => (source._1 -> source._2.toMap)
        }).toMap
      },
      upgradeScalaServer := {
        val log = streams.value.log
        log.info("About to upgrade unmanaged server.")
        val api: Api = apiImpl.value
        val dbc = databaseConnection.value
        val dataSource: org.postgresql.ds.PGSimpleDataSource = new org.postgresql.ds.PGSimpleDataSource() {
          setServerName(dbc("ServerName"))
          setPortNumber(dbc("Port").toInt)
          setDatabaseName(dbc("DatabaseName"))
          setUser(dbc("User"))
          setPassword(dbc("Password"))
          setSsl(false)
        }
        val upgrade = api.upgradeUnmanagedServer(token.value, dataSource, packageName.value, targetLanguages.value, sourceOptions.value, dslFiles.value)
        if (upgrade.authorized) log.info("Request for migration sources successful.") else sys.error(upgrade.authorizationErrorMessage)
        val migration = api.upgradeUnmanagedDatabase(dataSource, List(upgrade.migration))
        if (migration.successfulUpgrade) log.info("Database upgrade successful.") else sys.error("Migration failed!" + migration.databaseConnectionErrorMessage)
        mapAsScalaMap(upgrade.serverSource.map {
          source => (source._1 -> source._2.toMap)
        }).toMap
      },
      upgradeScalaServerUnmanaged := {
        val log = streams.value.log
        val source = upgradeScalaServer.value
        (interfacesOutputDirectory.value, servicesOutputDirectory.value) match {
          case (Some(iDir), Some(sDir)) =>
            source.foreach {
              case (lang: String, src: Map[String, String]) =>
                log.info("Writing " + src.size + " " + lang + " sources.")
                src.foreach {
                  case (sourceName, sourceContent) =>
                    val sourceFile: File = (if (sourceName.contains("postgres")) sDir / lang else iDir / lang) / sourceName
                    log.info("Writing " + sourceFile.getAbsolutePath)
                    IO.write(sourceFile, sourceContent, dslCharset.value, false)
                }
            }
          case _ =>
            outputDirectory.value match {
              case Some(outputDirectory) =>
                source.foreach {
                  case (lang: String, src: Map[String, String]) =>
                    log.info("Writing " + src.size + " " + lang + " sources.")
                    src.foreach {
                      case (sourceName, sourceContent) =>
                        val sourceFile: File = outputDirectory / lang / sourceName
                        log.info("Writing " + sourceFile.getAbsolutePath)
                        IO.write(sourceFile, sourceContent, dslCharset.value, false)
                    }
                }
            }
        }
      }
    )
}
