import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

username := "rinmalavi@gmail.com"

password := "qwe321"

dslProjectId := "6bff118e-0ad9-4aee-813d-b292df9b9291"

outputDirectory := Some(file("out"))

api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

packageName := "namespace"

TaskKey[Unit]("checkOut") := {
  val checkFile = outputDirectory.value.get / "scala" / "name" / "space" / "A" / "B.scala"
  assert(checkFile.exists())
}
