@echo off
setlocal

java ^
  -jar "%~dp0dsl-clc.jar" latest ^
  --project-ini-path=~\.config\dsl-compiler-client\dsl-project.ini ^
  --dsl-path="%~dp0model" ^
  --output-path="%~dp0..\client-api\model\src\generated" ^
  %*
