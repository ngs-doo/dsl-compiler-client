import sbt._

trait Dependencies {
  val slf4j         = "org.slf4j" % "slf4j-api" % "1.7.7"
  val logback       = "ch.qos.logback" % "logback-classic" % "1.1.2"

  val jodaTime      = "joda-time" % "joda-time" % "2.3"
  val jodaConvert   = "org.joda" % "joda-convert" % "1.5"

  val jUnit         = "junit" % "junit" % "4.11"
  val hamcrest      = "org.hamcrest" % "hamcrest-all" % "1.3"

  val postgresql    = "org.postgresql" % "postgresql" % "9.3-1101-jdbc41"

  val commonsCodec  = "commons-codec" % "commons-codec" % "1.9"
  val commonsIo     = "commons-io" % "commons-io" % "2.4"

  val config        = "com.typesafe" % "config" % "1.2.1"

  val jGit          =  "org.eclipse.jgit" % "org.eclipse.jgit" % "3.4.0.201405281120-rc2"
}
