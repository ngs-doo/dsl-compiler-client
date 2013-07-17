import sbt._
import Keys._

import sbtassembly.Plugin._
import AssemblyKeys._

trait Client extends Build with Default {
  protected def clientProject(id: String) = Project(
    id.toLowerCase
  , file(id.toLowerCase)
  , settings = javaSettings ++ Seq(
      version := "0.7.8-SNAPSHOT"
    , name := "DSL-Compiler-" + id
    , organization := "com.dslplatform"
    , initialCommands := "import com.dslplatform.compiler.client._"
    )
  )
}

object Client extends Client {
  lazy val apiLogging    = clientProject("Client-API-Logging") settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.0.13" % "provided"
    )
  )

  lazy val apiCommons    = clientProject("Client-API-Commons") inject(apiLogging)
  lazy val apiDiff       = clientProject("Client-API-Diff") inject(apiCommons)
  lazy val apiCache      = clientProject("Client-API-Cache") inject(apiCommons)

  lazy val apiTransport  = clientProject("Client-API-Transport")
  lazy val apiParams     = clientProject("Client-API-Params") inject(apiCommons)

  lazy val apiInterfaces = clientProject("Client-API-Interfaces") inject(apiTransport, apiParams)
  lazy val apiCore       = clientProject("Client-API-Core") inject(apiInterfaces, apiDiff, apiCache)

  lazy val cmdline       = clientProject("Client-CmdLine") inject(apiCore)
  lazy val gui           = clientProject("Client-GUI") inject(apiCore)

  lazy val client        = clientProject("Client-Assembly") inject(cmdline, gui) settings(
    assemblyPublishSettings ++ Seq(
      mainClass in assembly := Some("com.dslplatform.compiler.client.Main")
    , jarName   in assembly := "dsl-clc.jar"
    ): _*
  )

  lazy val launcher      = clientProject("Client-Launcher")
}
