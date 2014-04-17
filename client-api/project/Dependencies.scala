import sbt._

trait Dependencies {
  val slf4j         = "org.slf4j" % "slf4j-api" % "1.7.5"
  val slf4jSimple   = "org.slf4j" % "slf4j-simple" % "1.7.5"

  val jodaTime      = "joda-time" % "joda-time" % "2.3"
  val jodaConvert   = "org.joda" % "joda-convert" % "1.5"

  val jUnit         = "junit" % "junit" % "4.11"
  val postgresql    = "org.postgresql" % "postgresql" % "9.3-1101-jdbc4"
  val commonscodec  = "commons-codec" % "commons-codec" % "1.9"
  val config        = "com.typesafe" % "config" % "1.2.0"
}
