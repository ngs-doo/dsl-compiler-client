import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

projectPropsPath := Some(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

outputDirectory := Some(file("out"))

testProject := true

api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

TaskKey[Unit]("checkDiff") := {
  val diff = getDiffString.value
  streams.value.log.info(diff)
  assert(diff.contains("int i;"))
}
