@echo off

echo Firing up the Scala REPL ...
call "%~dp0sbt.bat" %* "set autoScalaLibrary := true" console
