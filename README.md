## Command line tool for interaction with DSL Platform

DSL Platform is compiler from Domain Specification Language to various target languages, such as Java, C#, Scala, PHP.
It also supports automated database migrations for Postgres and Oracle.

It's premise is to use Invasive software composition to write better software. By focusing on rich modeling supported on top of Object-relational databases development can be improved by automating creation and maintenance of various boilerplate found in today software development.

###How it works

Instead of writing *POJO*/*POCO*/*POPO*/... classes such as

    public class Post {
      [Key][NotNull][MaxLength(50)]
      public String Title { get; set; }
      [NotNull]
      public String Content { get; set; }
      public Set<String> Tags { get; set; }
    }

which are simple data structures mapped to some ORM/NoSQL thing, you can write actual model outside of the code with DSL:

    aggregate Post(Title) {
      string(50) Title;
      string Content;
      set<string(10)> Tags;
    }

which gives you DTO like above for free, but also gives you various improvements, such as better/faster serialization, support for rich models in ORDBMS, LINQ to SQL, same exact typesafe object in the database and various other complex features with a minimal description. 

While taking model outside of your code doesn't look very important, when you start making changes to your model, you'll benefit from letting the compiler do all the boring work and preparing migrations to support such changes.

###Getting started

Create an account on [dsl-platform](https://dsl-platform.com/). This allows you to use free online compiler.

Think about your domain - DSL is designed to get out of your way while you are modeling. Write some DSL which captures everything you need from it.

Choose a language and a compatible open source library to write Android/.NET/PHP/Java applications on top of your model. 

###Supported libraries
 
 * [Revenj.NET](https://github.com/ngs-doo/revenj)
 * [DSL Java client](https://github.com/ngs-doo/dsl-client-java)
 * [DSL PHP client](https://github.com/ngs-doo/dsl-client-php)
 * [DSL Scala client](https://github.com/ngs-doo/dsl-client-scala)

###How to use

Download [dsl-clc.jar](https://bitbucket.org/zapov/dsl-compiler-client/downloads/dsl-clc.jar) to your project folder and run it with java.

    java -jar dsl-clc.jar

This will display all available options of the tool and examples on how to use it.

###Compiled libraries

This tool is used to produce a compiled library which you can use to implement custom behavior and business logic. While you can use generated code instead of compiled library, this behavior is highly discouraged since it leads to broken development once you start modifying generated code.

###Usage examples

Compiling Java client library from DSL located in ./dsl folder

    java -jar dsl-clc.jar -target=java_client

Creating PHP source from DSL located in ./model folder

    java -jar dsl-clc.jar -target=php -dsl=model

Compiling Java client library and specifying output jar name

    java -jar dsl-clc.jar -java_client=./model.jar

Compiling Java client library, .NET server library and applying database migration using properties file *compile-options.props* with the content

    u=account@dsl-platform.com
    java_client=./play/model.jar
    revenj=./revenj/ServerModel.dll
    dsl=C:/Models/MyApp
    db=localhost/DB?user=postgres
    migration
	apply

and

    java -jar dsl-clc.jar -properties=compile-options.props

Displaying a diff between model previously applied to the database and current one

    java -jar dsl-clc.jar -diff -db=localhost/MyProject?user=user&password=password -dsl=modeling/dsl

Disabling prompt and forcing destructive migrations for nightly builds

    java -jar dsl-clc.jar -db=localhost/Project?user=user&password=password -no-prompt -migration -apply -force

Checking if current DSL is a valid one

    java -jar dsl-clc.jar -parse -u=my-account@dsl-platform

Saving SQL migration to specific folder

    java -jar dsl-clc.jar /migration /sql=sql-upgrade-scripts -db=localhost/Project?user=postgres

 