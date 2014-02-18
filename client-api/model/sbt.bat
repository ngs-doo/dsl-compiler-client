@echo off
setlocal

set DEFAULT=
if %1.==. set DEFAULT=shell

for %%? in ("%~dp0.") do set PROJECT=%%~n?

call "%~dp0..\sbt.bat" "project %PROJECT%" %DEFAULT%%*
