import sbt._
import Keys._

import com.typesafe.sbteclipse.plugin.EclipsePlugin.{ settings => eclipseSettings, _ }

object ClientApi extends Build with Default {
  protected def clientApiProject(id: String) = Project(
    id.toLowerCase
    , file(id.toLowerCase)
    , settings = javaSettings ++ Seq(
      name := "DSL-Compiler-Client-" + id
      , initialCommands := "import com.dslplatform.compiler.client._"
    )
  )

  lazy val core = clientApiProject("Core") inject(
      jodaTime
    , postgresql % "provided"
    , slf4j
    , commonscodec
    , slf4jSimple % "test"
    , jUnit % "test"
    ) settings(
      unmanagedSourceDirectories in Compile := Seq(
          sourceDirectory.value / "model" / "java"
        , sourceDirectory.value / "interface" / "java"
        , sourceDirectory.value / "service" / "java"
      )
    , unmanagedResourceDirectories in Compile := Seq(
      sourceDirectory.value / "main" / "resources"
    )
    , unmanagedResourceDirectories in Test := Seq(sourceDirectory.value / "test" / "resources")
    , EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
    )

  lazy val api = clientApiProject("Api") inject(
      slf4jSimple
    , jUnit % "test") dependsOn (core % "test->test;compile->compile")

  lazy val dslCompilerSBT = Project(
      "sbt"
    , file("sbt")
    ) settings (ScriptedPlugin.scriptedSettings: _*) settings (
      name := "DSL-Compiler-Client-SBT"
    , libraryDependencies ++= Seq(postgresql, config, jUnit % "test") // TODO - make mock scope
    , ScriptedPlugin.scriptedLaunchOpts := { ScriptedPlugin.scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
      }
    , unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value)
    , unmanagedSourceDirectories in Test := (unmanagedSourceDirectories in Test in core).value :+ (scalaSource in Test).value
    , unmanagedResourceDirectories in Test := (unmanagedResourceDirectories in Test in core).value
    , publishArtifact in (Test, packageBin) := true
    , publishLocal <<= publishLocal dependsOn (publishLocal in api, publishLocal in core)
    , sbtPlugin := true
    ) dependsOn (api)
}
