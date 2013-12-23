#!/bin/bash

echo Will join all projects into a single jar ...
`dirname $0`/sbt.sh --no-jrebel "$@" clean assembly
