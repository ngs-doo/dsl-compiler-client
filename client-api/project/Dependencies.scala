import sbt._

trait Dependencies {
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.5"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.5"

  val jodaTime = "joda-time" % "joda-time" % "2.3"
  val jodaConvert = "org.joda" % "joda-convert" % "1.5"

  val dslHttp = "com.dslplatform" % "dsl-client-http-apache" % "0.4.14-SNAPSHOT"
}
