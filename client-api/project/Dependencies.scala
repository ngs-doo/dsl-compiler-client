import sbt._

trait Dependencies {
  val slf4j   = "org.slf4j" % "slf4j-api" % "1.7.5"
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.13"

  val jodaTime    = "joda-time" % "joda-time" % "2.3"
  val jodaConvert = "org.joda" % "joda-convert" % "1.5"

  val jUnit = "junit" % "junit" % "4.11"
}
