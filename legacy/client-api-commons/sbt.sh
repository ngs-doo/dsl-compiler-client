#!/bin/bash

PROJECT_DIR=`dirname $(readlink -f $0)`
PROJECT=`basename $PROJECT_DIR` 

`dirname $(readlink -f $0)`/../sbt.sh "project $PROJECT" "$@"
