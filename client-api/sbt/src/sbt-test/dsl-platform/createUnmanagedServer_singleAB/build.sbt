// createUnmanagedServer_singleAB
import dslplatform.CompilerPlugin._
import DslKeys._

dslplatform.CompilerPlugin.dslSettings

username := "rinmalavi@gmail.com"

password := "qwe321"

dslProjectId := "6bff118e-0ad9-4aee-813d-b292df9b9291"

outputPathMapping := OutputPathMapping.interface_service_mapping("i", "s")

targetSources := Set() // TODO : add ScalaServer_S to mocks

packageName := "namespace"

api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

databaseConnection := Map("ServerName" -> "localhost", "Port" -> "5432", "DatabaseName" -> "dccTest", "User" -> "dccTest", "Password" -> "dccTest")

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
