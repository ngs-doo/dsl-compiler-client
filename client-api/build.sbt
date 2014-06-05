version in ThisBuild              := "0.20.0-SNAPSHOT"

organization in ThisBuild         := "com.dslplatform"

publishTo in ThisBuild            := Some(if (version.value endsWith "SNAPSHOT") Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)

licenses in ThisBuild             += ("BSD-style", url("http://opensource.org/licenses/BSD-3-Clause"))

startYear in ThisBuild            := Some(2013)

scmInfo in ThisBuild              := Some(ScmInfo(url("https://github.com/ngs-doo/dsl-compiler-client.git"), "scm:git:https://github.com/ngs-doo/dsl-compiler-client.git"))

pomExtra in ThisBuild             ~= (_ ++ {Developers.toXml})

publishMavenStyle in ThisBuild    := true

pomIncludeRepository in ThisBuild := { _ => false }

homepage in ThisBuild             := Some(url("https://dsl-platform.com/"))

credentials in ThisBuild          += Credentials(Path.userHome / ".config" / "sonatype" / "element.credentials")

packagedArtifacts                 := Map.empty
