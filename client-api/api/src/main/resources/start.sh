#!/bin/sh
cd "$$(dirname "$$0")"/bin
exec mono Revenj.Http.exe "$$@" > ../logs/mono.log 2>&1