All clients are currently under development.

[Register at dsl-platform](https://dsl-platform.com/)
 
[Java Command Line Client](client-api/cmdlineclient)

[SBT Plugin](client-api/sbt)

[Revenj](https://github.com/ngs-doo/revenj)

To be able to deploy install mono - readme TDB

Restart mono server with `sudo /etc/init.d/mono restart`

Check if it works at `http://<hostname>/Domain.svc/search/<some_module_name.some_root_name_at_that_module>`

Customize config at `Revenj.Http.exe.config`

To disable auth add `<add key="CustomAuth" value="Revenj.Http.NoAuth"/>`

To enable the security use same username and projectId as the one you will provide to the Bootstrap.init at the client library. Which you are using with the same model as the one you just deployed the server with.