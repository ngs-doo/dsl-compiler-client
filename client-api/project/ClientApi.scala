import sbt._
import Keys._

import com.typesafe.sbteclipse.plugin.EclipsePlugin.{ settings => eclipseSettings, _ }

object ClientApi extends Build with Default {
  protected def clientApiProject(id: String) = Project(
    id.toLowerCase
  , file(id.toLowerCase)
  , settings = javaSettings ++ Seq(
      name := "DSL-Compiler-Client-API-" + id
    , initialCommands := "import com.dslplatform.compiler.client._"
    )
  )

  lazy val util = clientApiProject("Util") inject(
    slf4j
  )

  lazy val params = clientApiProject("Params")

  lazy val cmdLineParser = clientApiProject("CmdLineParser") inject(
    slf4j
  , commonsIo
  , util
  , slf4jSimple % "test"
  , jUnit % "test"
  , params
  ) settings (
      unmanagedResourceDirectories in Test := Seq(sourceDirectory.value / "test" / "resources")
    , EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
  )

  lazy val core = clientApiProject("Core") inject(
      jodaTime
    , postgresql % "provided"
    , slf4j
    , commonsCodec
    , util
    , slf4jSimple % "test"
    , jUnit % "test"
    ) settings(
      unmanagedSourceDirectories in Compile := Seq(
        sourceDirectory.value / "interface" / "java"
      , sourceDirectory.value / "service" / "java"
      , sourceDirectory.value / "model" / "java"
      )
    , unmanagedResourceDirectories in Compile := Seq(
        sourceDirectory.value / "main" / "resources"
      )
    , unmanagedResourceDirectories in Test := Seq(
        sourceDirectory.value / "test" / "resources"
      )
    , EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
    )

  lazy val api = clientApiProject("Api") inject (
      core
    , slf4jSimple % "test"
    , jUnit % "test")
/*
  lazy val interface = clientApiProject("Interface") inject (
    // model
  //  slf4j
 // , slf4jSimple
  ) settings (
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "interface" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "service" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "model" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "config" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "launcher" / "java"
  ) dependsOn(model)
*/

}
