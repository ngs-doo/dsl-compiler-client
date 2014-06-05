import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

projectPropsPath := Some(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

outputDirectory := Some(file("out"))

api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

packageName := "namespace"

TaskKey[Unit]("checkOut") := {
  val checkFile = outputDirectory.value.get / "scala" / "namespace" / "A" / "B.scala"
  assert(checkFile.exists())
}
