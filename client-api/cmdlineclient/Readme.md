### CLC Java

To build an assembly call `assembly` from sbt console, and watch for the output.

Operations in this project assume that mono, java and postgres are installed.

## Usage 
Jar made this way will accept numerous arguments. The most useful is `-f` followed by a path to the file containing the rest of the properties.
This file can look something like this

    project-name=projectName
    username=email.you.registered@dsl-platform.com 
    password=password_chose_for_this
    project-id=<optional uuid of a project you wish to manage>
    api-url=<WIP>
    package-name=<namespaces of your choice>
    logging-level=DEBUG
    output-path=<where the client will write generated source>
    dsl-path=<where your model definition resides at>
    migration-file=<where migration file will be outputed at>
    revenj-path=<optional path to revenj library>
    mono-path=<optional path where to deploy mono to>
    skip-diff=<true or false - should the diff be skipped if its a part of a called task>
    db-username=<username with which to connect to the database> 
    db-password=<password with which to connect to the database>
    db-host=<database url> 
    db-port=<database port>
    db-database-name=<database name>
    db-connection-string=dbconnstring
    target=<target source language> 
    actions=<action to be performed>
    
     update, config, parse, diff, last-dsl, generate-sources, download-generated-model, unmanaged-sql-migration
    #, unmanaged-cs-server, unmanaged-source,

Target sources could be one or more of following:

    csharp_server, csharp, java, php, scala_server, csharp_client, java_client, csharp_portable, php_client, scala_client

Not all actions are implemented so far. Current focus is on the unmanaged projects with which you can do:

- `parseDSL` - test weather the provided dsl is parseable. Will output success if yes, or information on reasons of a failure to standard output.
- `diff` - outputs the difference between last and given dsl to standard output in unified format.
- `unmanaged-sources` - outputs the target source code to the location provided as `output-path`
- `unmanaged-sql-migration` - requests migration sql for difference between two dsls, the last one and the provided one. Migration SQL will be outputed to location provided as `migration-path` and can be used to upgrade the database from which the last dsl came from.
- `upgrade-unmanaged-database` - upgrades the database with a migration sql provided at the `migration-path` location, if such file does not exist migration will be requested from the compiler. 
- `deploy-unmanaged-server` - this action will do the parse and the diff action. If the parse is successful, user will be prompted if he/she likes the presented diff. On the positive response the application will request migration SQLs, server an optional client code then considering preferences upgrade the database and deploy the server. After a successful operation user will be informed to restart the mono service. 
