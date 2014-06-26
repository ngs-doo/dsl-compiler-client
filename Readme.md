All clients are currently under development.

[Register at dsl-platform](https://dsl-platform.com/)
 
[Java Command Line Client](client-api/cmdlineclient)

[SBT Plugin](client-api/sbt)

[Revenj](https://github.com/ngs-doo/revenj)

[Revenj download URL](https://github.com/ngs-doo/revenj/releases/download/1.0.1/http-server.zip)

Restart mono server with `sudo /etc/init.d/mono restart`

Service can now be found available at `http://<hostname>/Domain.svc/search/<some_module_name.some_root_name_at_that_module>`

Customize config at `Revenj.Http.exe.config`

To disable auth add `<add key="CustomAuth" value="Revenj.Http.NoAuth"/>`

To enable the security, use the same username and projectId as the one you will provide to the Bootstrap.init in the client code. 
Which you are using with the same model as the one you just deployed the server with.
