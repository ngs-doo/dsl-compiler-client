#!/bin/bash

echo Entering continuous test loop ...
`dirname $0`/sbt.sh --loop --no-jrebel "$@" ~test
