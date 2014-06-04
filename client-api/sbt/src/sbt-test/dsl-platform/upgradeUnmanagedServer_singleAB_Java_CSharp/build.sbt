import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

projectIniPath := Some(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

outputDirectory := Some(file("out"))

packageName := "namespace"

targetSources := Set("Java", "Scala")

monoDependencyFolder    := file(System.getProperty("user.home")) / "code" / "dsl_compiler_client_user" / "revenj"

//api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

TaskKey[Unit]("checkOut") := {
  val output = outputDirectory.value.get.listFiles()
  assert(output.map{_.getName()}.contains("java"))
  val checkSharpFile = monoTempFolder.value / "DatabaseRepositorymyModule" / "ARepository.cs"
  val checkJavaFile = outputDirectory.value.get / "java" / "namespace" / "myModule" / "A.java"
  assert(checkSharpFile.exists())
  assert(checkJavaFile.exists())
}
