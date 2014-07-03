#Plugin

###Quick Start

####Minimal build settings contain
1.1 `projectPropsPath` set to an option of a file looking something like this:

    dsl {
      username=<your username @ dsl-platform.com>,
      password=<your password for this username,
      projectId=<optional projectId for some tests>,
      package-name=<namespace of target sources>
    }
    db {
      ServerName=x,
      Port=x,
      DatabaseName=x,
      User=x,
      Password=x
    }
    
db part is and projectId are optional depending on a type of a project.

1.2 If `projectPropsPath` is not set then `username`, `password` and optional `projectId` can be set like this (in build.sbt):

    val credentials = com.typesafe.config.ConfigFactory.parseFile(file(System.getProperty("user.home")) / ".config" / "dsl-compiler-client" / "test.credentials")
    
    username := credentials.getString("dsl.username")
   
    password := credentials.getString("dsl.password")
   
    packageName := "namespace"  // if you wouldn't like to have the default namespace of "model".

2.1 Unmanaged projects need `revenj` in case you decide to compile or deploy with a plugin. Following keys must be set:

    monoDependencyFolder    := file(System.getProperty("user.home")) / "code" / "dsl_compiler_client_user" / "revenj"
   
    performDatabaseMigration  := false // or true if you would like a database to be upgraded
    
    performServerDeploy := true

this will probably change!

3.1 `targetSources` are by default sent to Set(Scala). Java, PHP, C# can also be added as client code.


##Settings in detail:

  - `username`: String  User name - setting it in a project will override the values red from the projectProps file

  - `password`: String  Password - setting it in a project will override the values red from the projectProps file.

  - `dslProjectId`: String - projectId - setting it in a project will override the values red from the projectProps file.

  - `packageName`: String - package name - setting it in a project will override the values red from the projectProps file.

  - `projectPropsPath: Option[File]` - Location of a projectProps definition. This file can hold username, password, projectId, namespace. Can be of form:

    dsl {
      username=<your username @ dsl-platform.com>,
      password=<your password for this username>,
      projectId=<optional projectId for some tests>,
      package-name=<namespace of target sources>
    }
    db {
      ServerName=x,
      Port=x,
      DatabaseName=x,
      User=x,
      Password=x
    }

  - `testProject: Boolean` - Is the project test project, this will automatically upgrade the database every time you change dsl.
                         - not implemented fully, not sure is it needed.

  - `dslCharset`: Charset - for dsl files

  - `projectConfiguration`: Map[String, String] - Project specific properties. This is set from a projectPropsFile

  - `outputDirectory`: Option[File] - Output directory for the client files.

  - `sourceOptions`: Set[String] - Options to generate source.

  - `outputPathMapping`: OutputPathMappingType - Maps a targetLanguage and an output source name to the output path, by the  this is set as `<outputDirectory> / targetSources / source name`.

  - `targetSources`: Set[String] - Languages option to generate source.

  - `dslDirectory`: PathFinder - Location of the dsl sources. By default this is dsl/*.dsl

  - `dslFiles`: Map[String, String] - Map of dsls and their names. Found in dslDirectories.

  - `databaseConnection` - Properties for a connection to the database.

#####Unmanaged project settings:

  - `migrationOutputFile`: Option[File] - If defined will write migration SQL to it.

  - `monoTempFolder` -  Place where to store C# files.

  - `monoDependencyFolder`: File - Path to the dependencies (revenj) for compilation of the C# files.

  - `monoServerLocation`: File - Location where mono application should be deployed to.

  - `generatedModel`: File - Compile target path, by default this is `<monoServerLocation>/bin/generatedModel.dll

  - `performDatabaseMigration`: Boolean - Should the database be upgraded automatically.

  - `performSourceCompile`: Boolean - Should `generatedModel` be compiled.

  - `performServerDeploy`: Boolean - Should the server be deployed automatically.

##Tasks:
#####Following tasks can be called

To have diff with last dsl be outputted to the console:

    getDiff

To have parse current dsl be outputted to the console:

    parseDSL 

Generate sources from a given dsl. Write it to the `outputDirectory`

    generateSourcesUnmanaged 

To upgrade managed projects database with a given dsl and generate sources. Writes resulting `targetSources` to `outputDirectory` or based on `outputPathMapping` if overridden.

    upgradeDatabaseUnmanaged 

To generate the C# server source

    generateUnmanagedCSSources 

To compile the generated sources into the `assembleName`. Will look for the sources in `monoTempFolder`. 
For compilation revenj lib is needed. There is one versioned under the
`dsl_compiler_client_user` project, and can be used till those sources become stable and exposed via api functionality.

    compileCSharpServer

Following tasks will upgrade the database and/or deploy the server based on keys `performDatabaseMigration`, `performSourceCompile`, `performServerDeploy` described above:

    upgradeScalaServer    
    upgradeCSharpServer
    upgradeUnmanagedDatabase

####Plugin Testing

To be able to preform system tests credentials for dsl-platform and database must be supplied.
They are loaded from `<user.home>/.config/dsl-compiler-client/test.credentials` and can look something like this:

    dsl {
      username=<your username @ dsl-platform.com>,
      password=<your password for this username,
      projectId=<optional projectId for some tests>,
      package-name=<namespace of target sources>
    }
    db {
      ServerName=x,
      Port=x,
      DatabaseName=x,
      User=x,
      Password=x
    }

db credentials are needed for testing an unmanaged functions.

To compile the C# sources revenj lib is needed. In the tests this is set to the revenj folder versioned under the
`dsl_compiler_client_user` project until those sources become stable and exposed via api functionality.

To run sbt test call 

    scripted

To run a single test call

    scripted dsl-platform/createUnmanagedServer_singleAB
    scripted dsl-platform/upgradeUnmanagedServer_singleAB
    scripted dsl-platform/upgradeUnmanagedServer_singleAB_Java_CSharp
    scripted dsl-platform/parseDSL
    scripted dsl-platform/getDiffManaged
    scripted dsl-platform/generateSourcesWithOutput
