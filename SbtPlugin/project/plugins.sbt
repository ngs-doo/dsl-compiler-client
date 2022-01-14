scalaVersion := "2.12.15"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:_",
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:-unused,_",
)

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
