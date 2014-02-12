@echo off

echo Entering continuous test loop ...
call "%~dp0sbt.bat" --loop --no-jrebel %* ~test
