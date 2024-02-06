sbtPlugin := true

organization := "com.dslplatform"
name := "sbt-dsl-platform"
version := "0.9.0"

scalaVersion := "2.12.18"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:_",
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:-unused,_",
)

libraryDependencies ++= Seq(
  "com.dslplatform" % "dsl-clc" % "2.1.0",
  "org.clapper" %% "classutil" % "1.5.1",
)

publishMavenStyle := false
licenses += (("BSD-style", url("http://opensource.org/licenses/BSD-3-Clause")))
startYear := Some(2016)
pomIncludeRepository := { _ => false }
homepage := Some(url("https://dsl-platform.com/"))
Test / publishArtifact := false
ThisBuild / versionScheme := Some("early-semver")

publishTo := Some(
  if (version.value endsWith "-SNAPSHOT")
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
scmInfo := Some(ScmInfo(
  url("https://github.com/ngs-doo/dsl-compiler-client/tree/master/SbtPlugin"),
  "scm:git:https://github.com/ngs-doo/dsl-compiler-client.git",
  Some("scm:git:git@github.com:ngs-doo/dsl-compiler-client.git"),
))
