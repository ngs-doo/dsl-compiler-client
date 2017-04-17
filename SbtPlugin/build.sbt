sbtPlugin := true

organization := "com.dslplatform"
name := "sbt-dsl-platform"

version := "0.5.1"

libraryDependencies ++= Seq(
  "com.dslplatform" % "dsl-clc" % "1.9.0",
  "org.clapper" %% "classutil" % "1.0.12"
)

publishMavenStyle := false
bintrayRepository := "sbt-dsl-platform"
licenses += (("BSD-style", url("http://opensource.org/licenses/BSD-3-Clause")))
startYear := Some(2016)
bintrayVcsUrl := Some("git@github.com:ngs-doo/dsl-compiler-client.git")
pomIncludeRepository := { _ => false }
homepage := Some(url("https://dsl-platform.com/"))
bintrayOrganization := Some("dsl-platform")
