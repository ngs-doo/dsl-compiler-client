import sbt._
import Keys._

trait Default {
  private object Repositories {
    val NGSNexus     = "NGS Nexus"     at "http://ngs.hr/nexus/content/groups/public/"
    val NGSReleases  = "NGS Releases"  at "http://ngs.hr/nexus/content/repositories/releases/"
    val NGSSnapshots = "NGS Snapshots" at "http://ngs.hr/nexus/content/repositories/snapshots/"
  }

//  ---------------------------------------------------------------------------

  private object Resolvers {
    import Repositories._

    lazy val settings = Seq(
      resolvers := Seq(NGSNexus, NGSSnapshots)
    , externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false)
    )
  }

//  ---------------------------------------------------------------------------

  private object Publishing {
    import Repositories._

    val settings = Seq(
      publishTo := Some(
        if (version.value endsWith "SNAPSHOT") NGSSnapshots else NGSReleases
      )
    , credentials += Credentials(Path.userHome / ".config" / "ngs-util_master" / "nexus.config")
    )
  }

//  ---------------------------------------------------------------------------

  import com.typesafe.sbteclipse.plugin.EclipsePlugin.{ settings => eclipseSettings, _ }
  import net.virtualvoid.sbt.graph.Plugin._
  import sbtassembly.Plugin._, AssemblyKeys._

  lazy val scalaSettings =
    Defaults.defaultSettings ++
    Resolvers.settings ++
    Publishing.settings ++
    eclipseSettings ++
    graphSettings ++
    assemblySettings ++ Seq(
      javaHome := sys.env.get("JDK16_HOME").map(file(_))
    , javacOptions := Seq(
        "-deprecation"
      , "-encoding", "UTF-8"
      , "-Xlint:unchecked"
      , "-source", "1.6"
      , "-target", "1.6"
      )

    , crossScalaVersions := Seq("2.10.2")
    , scalaVersion := crossScalaVersions.value.head
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

    , unmanagedSourceDirectories in Compile := (scalaSource in Compile).value :: Nil
    , unmanagedSourceDirectories in Test := (scalaSource in Test).value :: Nil
    , libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.9.1" % "test"
      , "junit" % "junit" % "4.11" % "test"
      )

    , publishArtifact in (Compile, packageDoc) := false
    , EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

    , fork in run := true
    , parallelExecution in Test := false
    )

  lazy val javaSettings = scalaSettings ++ Seq(
      autoScalaLibrary := false
    , crossPaths := false
    , unmanagedSourceDirectories in Compile := (javaSource in Compile).value :: Nil
    , unmanagedSourceDirectories in Test := (javaSource in Test).value :: (scalaSource in Test).value :: Nil
    )

  lazy val assemblyPublishSettings = assemblySettings ++ Seq(
      artifact in (Compile, assembly) ~= (_.copy(`classifier` = Some("assembly")))
    , test in assembly := {}
    ) ++ addArtifact(artifact in (Compile, assembly), assembly)

//  ---------------------------------------------------------------------------

  implicit def pimpMyProjectHost(project: Project) =
    new PimpedProjectHost(project)

  case class PimpedProjectHost(project: Project) {
    def inject(children: ProjectReferencePlus*): Project = {
      children.toList match {
        case Nil =>
          project

        case head :: tail =>
          PimpedProjectHost(head attachTo project).inject(tail: _*)
      }
    }
  }

  sealed trait ProjectReferencePlus {
    def attachTo(attachment: Project): Project
  }

  implicit def pimpMyProject(attachment: Project): ProjectReferencePlus =
    new PimpedProject(attachment)

  case class PimpedProject(attachment: Project) extends ProjectReferencePlus {
    def attachTo(original: Project) = original dependsOn attachment
  }

  implicit def pimpMyProjectRef(attachment: ProjectRef): ProjectReferencePlus =
    new PimpedProjectRef(attachment)

  case class PimpedProjectRef(attachment: ProjectRef) extends ProjectReferencePlus {
    def attachTo(original: Project) = original dependsOn attachment
  }

  implicit def pimpMyModuleID(attachment: ModuleID): ProjectReferencePlus =
    new PimpedModuleID(attachment)

  case class PimpedModuleID(attachment: ModuleID) extends ProjectReferencePlus {
    def attachTo(original: Project) = original settings(
      libraryDependencies += attachment
    )
  }
}
