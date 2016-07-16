sbtPlugin := true

organization := "com.dslplatform"
name := "sbt-dsl-platform"

version := "0.1"

libraryDependencies += "com.dslplatform" % "dsl-clc" % "1.7.2"

publishMavenStyle := false

publishTo := Some(if (version.value endsWith "-SNAPSHOT") Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)
licenses += (("BSD-style", url("http://opensource.org/licenses/BSD-3-Clause")))
startYear := Some(2016)
scmInfo := Some(ScmInfo(url("https://github.com/ngs-doo/dsl-compiler-client.git"), "scm:git:https://github.com/ngs-doo/dsl-compiler-client.git"))
pomIncludeRepository := { _ => false }
homepage := Some(url("https://dsl-platform.com/"))
