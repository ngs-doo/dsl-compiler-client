import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

username := "rinmalavi@gmail.com"

password := "qwe321"

dslProjectId := "6bff118e-0ad9-4aee-813d-b292df9b9291"

interfacesOutputDirectory := Some(file("i"))

servicesOutputDirectory := Some(file("s"))

packageName := "namespace"

apiImpl := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

databaseConnection := Map("ServerName" -> "localhost", "Port" -> "5432", "DatabaseName" -> "dccTest", "User" -> "dccTest", "Password" -> "testingTest3")

TaskKey[Unit]("checkOut") := {
  val interfacesOutput = interfacesOutputDirectory.value.get.listFiles()
  val servicesOutput = servicesOutputDirectory.value.get.listFiles()
  assert(interfacesOutput.map{_.getName()}.contains("scala"))
  assert(servicesOutput.map{_.getName()}.contains("scala"))
  val checkFile = interfacesOutputDirectory.value.get / "scala" / "namespace" / "SystemConfiguration.scala"
  assert(checkFile.exists())
}
