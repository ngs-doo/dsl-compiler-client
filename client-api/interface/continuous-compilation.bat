@echo off

echo Entering continuous compilation loop ...
call "%~dp0sbt.bat" --loop --no-jrebel %* ~compile
