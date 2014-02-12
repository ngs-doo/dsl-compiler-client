#!/bin/sh

dir="$( dirname "$(readlink -f "$0")" )"

echo Will publish all components and the assembly ...
"$dir/sbt.sh" clean compile publish
