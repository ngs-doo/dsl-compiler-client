@echo off

echo Will publish all components and the assembly ...
call "%~dp0sbt.bat" clean compile publish
