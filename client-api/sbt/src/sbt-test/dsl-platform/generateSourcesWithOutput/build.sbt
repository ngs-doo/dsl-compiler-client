import dslplatform.CompilerPlugin.DslKey._

dslplatform.CompilerPlugin.dslSettings

username := "rinmalavi@gmail.com"

password := "qwe321"

dslProjectId := "6bff118e-0ad9-4aee-813d-b292df9b9291"

outputDirectory := Some(file("out"))

packageName := "namespace"

TaskKey[Unit]("checkOut") := {
  val output = outputDirectory.value.get.listFiles()
  val checkFile = interfacesOutputDirectory.value.get / "scala" / "namespace" / "SystemConfiguration.scala"
  assert(checkFile.exists())
}
