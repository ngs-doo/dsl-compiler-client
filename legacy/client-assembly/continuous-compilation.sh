#!/bin/bash

echo Entering continuous compilation loop ...
`dirname $0`/sbt.sh --loop --no-jrebel "$@" ~compile
