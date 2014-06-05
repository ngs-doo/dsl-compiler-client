import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

projectPropsPath := Some(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

outputDirectory := Some(file("out"))

TaskKey[Unit]("checkParseDSL") := {
  assert(parseDSL.value, "Did not parse")
}
