resolvers := Seq(
  "NGS Nexus" at "http://ngs.hr/nexus/content/groups/public/"
, Resolver.url("NGS Nexus (Ivy)",
    url("http://ngs.hr/nexus/content/groups/public/"))(Resolver.ivyStylePatterns)
)

externalResolvers <<= resolvers map { r =>
  Resolver.withDefaultResolvers(r, mavenCentral = false)
}

// =======================================================================================

// +-------------------------------------------------------------------------------------+
// | SBT Eclipse (https://github.com/typesafehub/sbteclipse)                             |
// | Creates .project and .classpath files for easy Eclipse project imports              |
// |                                                                                     |
// | See also: Eclipse downloads (http://www.eclipse.org/downloads/)                     |
// | See also: Scala IDE downloads (http://download.scala-ide.org/)                      |
// +-------------------------------------------------------------------------------------+

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.2.0")

// +-------------------------------------------------------------------------------------+
// | Dependency graph SBT plugin (https://github.com/jrudolph/sbt-dependency-graph)      |
// | Lists all library dependencies in a nicely formatted way for easy inspection.       |
// +-------------------------------------------------------------------------------------+

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

// +-------------------------------------------------------------------------------------+
// | SBT Assembly (https://github.com/sbt/sbt-assembly)                                  |
// | Creates single jar releases from multiple projects                                  |
// +-------------------------------------------------------------------------------------+

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.1")
