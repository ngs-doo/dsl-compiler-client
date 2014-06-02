// createUnmanagedServer_singleAB
import dslplatform.CompilerPlugin._
import DslKeys._

dslplatform.CompilerPlugin.dslSettings

val testCredentials = com.typesafe.config.ConfigFactory.parseFile(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

username := testCredentials.getString("dsl.username")

password := testCredentials.getString("dsl.password")

dslProjectId := testCredentials.getString("dsl.projectId")

outputPathMapping := OutputPathMapping.interface_service_mapping("i", "s")

targetSources := Set() // TODO : add ScalaServer_S to mocks

packageName := "namespace"

api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

databaseConnection := Map(
  "ServerName"    -> testCredentials.getString("db.ServerName"),
  "Port"          -> testCredentials.getString("db.Port"),
  "DatabaseName"  -> testCredentials.getString("db.DatabaseName"),
  "User"          -> testCredentials.getString("db.User"),
  "Password"      -> testCredentials.getString("db.Password"))

TaskKey[Unit]("checkOut") := {
  val interfacesOutput = file("i").listFiles()
  val servicesOutput = file("s").listFiles()
  assert(interfacesOutput.map{_.getName()}.contains("scala"))
  assert(servicesOutput.map{_.getName()}.contains("scala"))
  val checkFile1 = file("i") / "scala" / "namespace" / "SystemConfiguration.scala"
  assert(checkFile1.exists())
  val checkFile2 = file("i") / "scala" / "namespace" / "myModule" / "A.scala"
  assert(checkFile2.exists())
}
