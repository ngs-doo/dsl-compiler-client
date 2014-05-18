import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

username := "rinmalavi@gmail.com"

password := "qwe321"

dslProjectId := "6bff118e-0ad9-4aee-813d-b292df9b9291"

outputDirectory := Some(file("out"))

TaskKey[Unit]("checkParseDSL") := {
  assert(parseDSL.value, "Did not parse")
}
