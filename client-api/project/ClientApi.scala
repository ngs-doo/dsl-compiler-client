import sbt._
import Keys._

import com.typesafe.sbteclipse.plugin.EclipsePlugin.{settings => eclipseSettings, _}
import sbtassembly.Plugin._
import AssemblyKeys._

object ClientApi extends Build with Default {
  protected def clientApiProject(id: String) = Project(
    id.toLowerCase
    , file(id.toLowerCase)
    , settings = javaSettings ++ Seq(
        name := "DSL-Compiler-Client-" + id
      , initialCommands := "import com.dslplatform.compiler.client._"
    )
  )

  lazy val util = clientApiProject("Util") inject(
    slf4j, commonsIo
    )

  lazy val params = clientApiProject("Params")

  lazy val cmdLineParser = clientApiProject("CmdLineParser") inject(
      slf4j
    , commonsIo
    , util
    , logback % "test"
    , jUnit % "test"
    , params
    ) settings(
        unmanagedResourceDirectories in Test := Seq(sourceDirectory.value / "test" / "resources")
      , EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
    )

  lazy val cmdLineClient = clientApiProject("CmdLineClient") inject(
    cmdLineParser,
    slf4jSimple,
    util
    ) dependsOn (api % "test->test;compile->compile") settings(
    assemblySettings: _*) settings (
      artifact in (Compile, assembly) ~= (_.copy(`classifier` = Some("assembly")))
      , test in assembly := {}
      , mainClass in assembly := Some("com.dslplatform.compiler.client.cmdline.Main")
      , jarName   in assembly := "dsl-clc.jar"
      , test      in assembly := {}
    )

  lazy val core = clientApiProject("Core") inject(
      jodaTime
    , postgresql //% "provided"
    , slf4j
    , commonsCodec
    , util
    , logback % "test"
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
    , unmanagedResourceDirectories in Test := Seq(sourceDirectory.value / "test" / "resources")
    , EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
    )

  lazy val api = clientApiProject("Api") inject(
      commonsIo
    , jGit
    , logback % "test"
    , postgresql % "test"
    , jUnit % "test"
    , hamcrest % "test") dependsOn (core % "test->test;compile->compile", params)

  lazy val dslCompilerSBT = Project(
      "sbt"
    , file("sbt")
    ) settings (ScriptedPlugin.scriptedSettings: _*) settings(
      name := "DSL-Compiler-Client-SBT"
    , libraryDependencies ++= Seq(postgresql, config, logback, jUnit % "test")
    , ScriptedPlugin.scriptedLaunchOpts := {
        ScriptedPlugin.scriptedLaunchOpts.value ++
          Seq("-Xmx1024M"
            , "-XX:MaxPermSize=256M"
            , "-Dplugin.version=" + version.value
          )
      }
    , unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value)
    , unmanagedSourceDirectories in Test := (unmanagedSourceDirectories in Test in core).value :+ (scalaSource in Test).value
    , unmanagedResourceDirectories in Test := (unmanagedResourceDirectories in Test in core).value
    , publishArtifact in(Test, packageBin) := true
    , publishLocal <<= publishLocal dependsOn( /* publishLocal in params,*/ publishLocal in core, publishLocal in api, publishLocal in util)
    , sbtPlugin := true
    ) dependsOn (api)
}
