To be able to preform system tests credentials for dsl-platform and database must be supplied.
they are loaded from `<user.home>/.config/dsl-compiler-client/test.credentials` and look something like this:

    dsl {
      username=<your username @ dsl-platfrom.com>,
      password=<your password for this username,
      projectId=<optional projectId for some tests>
    }

db credentials are needed for testing an unmanaged functions.

    db {
      ServerName=x,
      Port=x,
      DatabaseName=x,
      User=x,
      Password=x
    }

To compile the C# sources revenj lib is needed. In the tests this is set to revenj folder versioned under the
`dsl_compiler_client_user` project until those sources become stable and exposed via api functionality.


To run sbt test call `scripted`

To run a single test call

    scripted dsl-platform/createUnmanagedServer_singleAB
    scripted dsl-platform/upgradeUnmanagedServer_singleAB
    scripted dsl-platform/upgradeUnmanagedServer_singleAB_Java_CSharp
    scripted dsl-platform/parseDSL
    scripted dsl-platform/generateSourcesWithOutput
