import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

val testCredentials = com.typesafe.config.ConfigFactory.parseFile(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

username := testCredentials.getString("dsl.username")

password := testCredentials.getString("dsl.password")

outputDirectory := Some(file("out"))

packageName := "namespace"

targetSources := Set()//"Java", "Scala")

monoDependencyFolder    := file(System.getProperty("user.home")) / "code" / "dsl_compiler_client_user" / "revenj"

//api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

databaseConnection := Map(
  "ServerName"    -> testCredentials.getString("db.ServerName"),
  "Port"          -> testCredentials.getString("db.Port"),
  "DatabaseName"  -> testCredentials.getString("db.DatabaseName"),
  "User"          -> testCredentials.getString("db.User"),
  "Password"      -> testCredentials.getString("db.Password"))

TaskKey[Unit]("checkOut") := {
  val output = outputDirectory.value.get.listFiles()
  assert(output.map{_.getName()}.contains("java"))
  val checkSharpFile = monoTempFolder.value / "DatabaseRepositorymyModule" / "ARepository.cs"
  val checkJavaFile = outputDirectory.value.get / "java" / "namespace" / "myModule" / "A.java"
  assert(checkSharpFile.exists())
  assert(checkJavaFile.exists())
}
