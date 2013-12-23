@echo off

echo Will join all projects into a single jar ...
call "%~dp0sbt.bat" --no-jrebel %* clean assembly
