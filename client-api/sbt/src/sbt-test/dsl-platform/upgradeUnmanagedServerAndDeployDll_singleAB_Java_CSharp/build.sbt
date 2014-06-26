import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

val testCredentials = com.typesafe.config.ConfigFactory.parseFile(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

username := testCredentials.getString("dsl.username")

password := testCredentials.getString("dsl.password")

outputDirectory := Some(file("out"))

packageName := "namespace"

targetSources := Set("Java")

monoDependencyFolder    := file(System.getProperty("user.home")) / "code" / "dcc-java-user" / "revenj"

api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

performServerDeploy := true

TaskKey[Unit]("checkOut") := {
  val startScript = monoServerLocation.value / "start.sh"
  val monoBinGen = monoServerLocation.value / "bin" / "generatedModel.dll"
  val monoBinExe = monoServerLocation.value / "bin" / "Revenj.Http.exe"
  val monoBinConfig = monoServerLocation.value / "bin" / "Revenj.Http.exe.config"
  assert(startScript.exists())
  assert(monoBinGen.exists())
  assert(monoBinExe.exists())
  assert(monoBinConfig.exists())
}
