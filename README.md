## Command line tool for interaction with DSL Platform

DSL Platform is a compiler from Domain Specification Language to various target languages, such as Java, C#, Scala, PHP.
It also supports automated database migrations for Postgres and Oracle.

It's premise is to use Invasive software composition to write better software.
By focusing on rich modeling supported on top of Object-relational databases development can be improved by automating creation and maintenance of various boilerplate found in today software development.

###How DSL works

Domain is described using various modeling building blocks, for example:

    module Domain {
      aggregate Document {
        string title { index; }
        Set<string(10)> tags;
        List<Page> pages;
        persistence { history; }
        timestamp createdOn;
      }
      value Page {
        date? frozen;
        Category category;
        string[] sentences;
        decimal(1) rating;
        Queue<string> notes;
      }
      enum Category {
        TopSecret;
        InternalOnly;
        PublicDomain;
      }
      snowflake<Document> DocumentList {
        title;
        createdOn;
        order by createdOn desc;
      }
    }

Which gives you a DTO/POCO/POJO/POPO/POSO... across different languages,
tables, types, views, functions specialized for supported ORDBMS,
repositories, converters, serialization and various other boilerplate required by the supported frameworks.
There are also a lot more modeling concepts which go beyond basic persistence features and cover reporting/customization/high performance aspects of the application.

The biggest benefit shows when you start changing your model and DSL compiler gives you not only the new dll/jar,
but also a SQL migration file which tries to preserve data if possible.
SQL migration is created by the compiler analysing differences between models, so you don't need to write manual migration scripts.

DSL compiler acts as a developer in your team which does all the boring work you would need to do, while providing high quality and high performance parts of the system.

###Getting started

Browse [dsl-platform](https://dsl-platform.com/) to get a feeling of supported DSL constructs. Compiler comes with a free offline version, but it requires Mono/.NET on the system.

Think about your domain - DSL is designed to get out of the way while modeling. Write some DSL which captures everything you need from it.

Choose a language and a compatible open source library to write Android/.NET/PHP/Java applications on top of your model.

###Supported libraries

 * [Revenj](https://github.com/ngs-doo/revenj)
 * [DSL Java/Android client](https://github.com/ngs-doo/dsl-client-java)
 * [DSL PHP client](https://github.com/ngs-doo/dsl-client-php)
 * [DSL Scala client](https://github.com/ngs-doo/dsl-client-scala)

###How to use

Download dsl-clc.jar to your project folder and run it with java.

    java -jar dsl-clc.jar

This will display all available options of the tool and examples on how to use it.

###Compiled libraries

This tool is used to produce a compiled library which you can use to implement custom behavior and business logic.
While you can use generated code instead of compiled library, this behavior is highly discouraged since it leads to broken development once you start modifying generated code.

###Usage examples

Compiling Java client library from DSL located in ./dsl folder

    java -jar dsl-clc.jar target=java_client

Compiling Java client library from DSL located in ./dsl folder using an offline compiler (you will need Mono or .NET to use offline compiler)

    java -jar dsl-clc.jar target=java_client compiler

Creating PHP source from DSL located in ./model folder

    java -jar dsl-clc.jar target=php dsl=model

Compiling Java client library and specifying output jar name

    java -jar dsl-clc.jar java_client=./model.jar

Compiling Java client library, .NET server library and applying database migration using properties file *compile-options.props* with the content

    u=account@dsl-platform.com
    java_client=./play/model.jar
    revenj.net=./revenj/ServerModel.dll
    dsl=C:/Models/MyApp
    postgres=localhost/DB?user=postgres&password=secret
    migration
    apply

and

    java -jar dsl-clc.jar properties=compile-options.props

Displaying a diff between model previously applied to the database and current one

    java -jar dsl-clc.jar diff postgres=localhost/MyProject?user=user dsl=modeling/dsl

Disabling prompt and forcing destructive migrations for nightly builds

    java -jar dsl-clc.jar "postgres=localhost/Project?user=user&password=password" no-prompt migration apply force

Checking if current DSL is a valid one

    java -jar dsl-clc.jar parse u=my-account@dsl-platform

Saving SQL migration to specific folder with a specific compiler version

    java -jar dsl-clc.jar migration sql=sql-upgrade-scripts postgres=localhost/Project?user=postgres compiler=/usr/dsl-compiler-v1.0/dsl-compiler.exe
