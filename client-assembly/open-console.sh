#!/bin/bash

echo Firing up the Scala REPL ...
`dirname $0`/sbt.sh "$@" 'set autoScalaLibrary := true' console
