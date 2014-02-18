import sbt._
import Keys._

trait ClientApi extends Build with Default {
  protected def clientApiProject(id: String) = Project(
    id.toLowerCase
  , file(id.toLowerCase)
  , settings = javaSettings ++ Seq(
      name := "DSL-Compiler-Client-API-" + id
    , initialCommands := "import com.dslplatform.compiler.client._"
    )
  )
}

object ClientApi extends ClientApi {
  lazy val model = clientApiProject("Model") inject (
    dslHttp
  ) settings (
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "generated" / "java"
  )

  lazy val interface = clientApiProject("Interface") inject (
    // model
    slf4j
  , slf4jSimple
  ) settings (
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "interface" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "service" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "model" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "config" / "java",
    unmanagedSourceDirectories in Compile += sourceDirectory.value / "launcher" / "java"
  ) dependsOn(model)
}
