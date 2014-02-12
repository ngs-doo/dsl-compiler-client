DSL Platform - Compiler Client
=================================

This project provides a simple way to compile your DSL Platform projects.
One can use its components as an API or simply use the assembly as an application.

There are a couple of projects in here, mostly separated by the functionality which they provide:

1. API
  * client-api-logging - an abstraction for logging
  * client-api-commons - IO and File utils
  * client-api-cache - authentication cache management
  * client-api-diff - file diff based on content hashes
  * client-api-params - parameters for initiating the compilation process
  * client-api-transport - messages which are being streamed back from the server
  * **client-api-interface** - the API towards the actual DSL Platform 
  * client-api-core - implementation of some core functionalities for usage in various clients

2. Clients
  * client-cmdline - the commandline client
  * client-gui - the Swing GUI client

3. Release
  * **client-assembly** - publication and project assembly
  * client-launcher - a tiny URL classloader for the client assembly

This project is licensed under the 3-clause BSD license.
