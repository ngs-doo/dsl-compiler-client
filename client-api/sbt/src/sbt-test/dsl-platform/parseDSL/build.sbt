import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

val testCredentials = com.typesafe.config.ConfigFactory.parseFile(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

username := testCredentials.getString("dsl.username")

password := testCredentials.getString("dsl.password")

dslProjectId := testCredentials.getString("dsl.projectId")

outputDirectory := Some(file("out"))

TaskKey[Unit]("checkParseDSL") := {
  assert(parseDSL.value, "Did not parse")
}
