import sbt._
import Keys._

import sbtassembly.Plugin._
import AssemblyKeys._

trait Client extends Build with Default {
  protected def clientProject(id: String) = Project(
    id.toLowerCase
  , file(id.toLowerCase)
  , settings = javaSettings ++ Seq(
      version := "0.7.11"
    , name := "DSL-Compiler-" + id
    , organization := "com.dslplatform"
    , initialCommands := "import com.dslplatform.compiler.client._"
    )
  )
}

object Client extends Client {
  lazy val apiLogging    = clientProject("Client-API-Logging")

  lazy val apiCommons    = clientProject("Client-API-Commons") inject(apiLogging)
  lazy val apiDiff       = clientProject("Client-API-Diff") inject(apiCommons)
  lazy val apiCache      = clientProject("Client-API-Cache") inject(apiCommons)

  lazy val apiTransport  = clientProject("Client-API-Transport")
  lazy val apiParams     = clientProject("Client-API-Params") inject(apiCommons)

  lazy val apiInterface  = clientProject("Client-API-Interface") inject(apiTransport, apiParams)
  lazy val apiCore       = clientProject("Client-API-Core") inject(apiInterface, apiDiff, apiCache)

  lazy val cmdline       = clientProject("Client-CmdLine") inject(apiCore) settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.5" % "provided"
    , "org.apache.logging.log4j" % "log4j-api" % "2.0-beta8" % "provided"
    , "org.fusesource.jansi" % "jansi" % "1.11" % "provided"
    , "jline" % "jline" % "2.10" % "provided"
    )
  )
  lazy val gui           = clientProject("Client-GUI") inject(apiCore)

  lazy val release       = clientProject("Client-Assembly") inject(cmdline, gui) settings(
    assemblyPublishSettings ++ Seq(
      mainClass in assembly := Some("com.dslplatform.compiler.client.Main")
    , jarName   in assembly := "dsl-clc.jar"
    , test      in assembly := {}
    ): _*
  )

  lazy val launcher      = clientProject("Client-Launcher")
}
