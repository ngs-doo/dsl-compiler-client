@echo off

echo Publishing the project ...
call "%~dp0sbt.bat" --no-jrebel %* clean publish
