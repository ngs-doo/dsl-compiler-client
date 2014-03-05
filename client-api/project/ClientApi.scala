import sbt._
import Keys._

object ClientApi extends Build with Default {
  protected def clientApiProject(id: String) = Project(
    id.toLowerCase
  , file(id.toLowerCase)
  , settings = javaSettings ++ Seq(
      name := "DSL-Compiler-Client-API-" + id
    , initialCommands := "import com.dslplatform.compiler.client._"
    )
  )

  lazy val core = Project(
      "core"
    , file("core")
    , settings = Defaults.defaultSettings ++ Seq(
      name := "DSL-Compiler-Client-API-Core"
      , initialCommands := "import com.dslplatform.compiler.client._"
      , javaHome := sys.env.get("JAVA_HOME").map(file(_))
      , javacOptions := Seq(
          "-deprecation"
        , "-encoding", "UTF-8"
        , "-Xlint:unchecked"
        , "-source", "1.6"
        , "-target", "1.6"
        )
      , crossScalaVersions := Seq("2.10.4-RC2")
      , scalaVersion := crossScalaVersions.value.head
      , autoScalaLibrary := false
      , scalacOptions := Seq(
          "-unchecked"
        , "-deprecation"
        , "-optimise"
        , "-encoding", "UTF-8"
        , "-Xcheckinit"
        , "-Yclosure-elim"
        , "-Ydead-code"
        , "-Yinline"
        , "-Xmax-classfile-name", "72"
        , "-Yrepl-sync"
        , "-Xlint"
        , "-Xverify"
        , "-Ywarn-all"
        , "-feature"
        , "-language:postfixOps"
        , "-language:implicitConversions"
        , "-language:existentials"
        , "-Yinline-warnings"
      )
      , libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % "1.0.10",
        "junit" % "junit" % "4.11" % "test"
        )

      , unmanagedSourceDirectories in Compile := (javaSource in Compile).value :: Nil
      , unmanagedSourceDirectories in Test := (javaSource in Test).value :: Nil
    )
  ) dependsOn(interface)

  lazy val model = clientApiProject("Model") inject (
    dslHttp
  ) settings (
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "generated" / "java"
  )

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
}
