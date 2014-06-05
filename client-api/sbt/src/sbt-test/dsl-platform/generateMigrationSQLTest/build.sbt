import dslplatform.CompilerPlugin.DslKeys._

dslplatform.CompilerPlugin.dslSettings

projectPropsPath := Some(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")

packageName := "namespace"

targetSources := Set("Java")

monoDependencyFolder    := file(System.getProperty("user.home")) / "code" / "dsl_compiler_client_user" / "revenj"

migrationOutputFile       := Some(file("migration.sql"))

performDatabaseMigration  := false
//api := new com.dslplatform.compiler.client.ApiImpl(new com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl(), new com.dslplatform.compiler.client.api.core.mock.HttpTransportMock(), com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock.mock_single_integrated)

TaskKey[Unit]("checkOut") := {
  val migrationfile = migrationOutputFile.value.get
  assert(migrationfile.exists())
  val migration = IO.read(migrationfile, dslCharset.value)
  assert(migration.contains("myModule"))
}
