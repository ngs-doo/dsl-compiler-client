@echo off
setlocal
pushd
cd "%~dp0"

set JVM_PARAMS=-Xss2m -Xms2g -Xmx2g -XX:+TieredCompilation -XX:ReservedCodeCacheSize=256m -XX:MaxPermSize=256m -XX:+CMSClassUnloadingEnabled -XX:+UseNUMA -XX:+UseParallelGC -Dscalac.patmat.analysisBudget=off

set TRY_JREBEL=true
set LOG_LEVEL=
set NO_PAUSE=false
set DO_LOOP=false

:PARSER_LOOP
if "%~1"=="" goto :PARSER_END

if "%~1"=="--jvm" (
  echo Setting JVM param [%~2]
  set JVM_PARAMS=%JVM_PARAMS% -D%~2
  shift
  goto :PARSER_CONTINUE
)

if "%~1"=="--debug" (
  echo "Setting debug mode"
  set LOG_LEVEL="set logLevel:=Level.Debug"
  goto :PARSER_CONTINUE
)

if "%~1"=="--prod" (
  echo Setting production mode
  set LOG_LEVEL="set logLevel:=Level.Info"
  goto :PARSER_CONTINUE
)

if "%~1"=="--no-jrebel" (
  echo Disabling JRebel for faster compilation
  set TRY_JREBEL=false
  goto :PARSER_CONTINUE
)

if "%~1"=="--loop" (
  echo Will run SBT in loop mode
  set DO_LOOP=true
  goto :PARSER_CONTINUE
)

if "%~1"=="--no-pause" (
  echo Will not pause in loop mode
  set NO_PAUSE=true
  goto :PARSER_CONTINUE
)

set SBT_PARAMS=%SBT_PARAMS% %1

:PARSER_CONTINUE
shift
goto :PARSER_LOOP
:PARSER_END

if %TRY_JREBEL%.==true. (
  if exist "%JREBEL_HOME%\jrebel.jar" set JVM_PARAMS=%JVM_PARAMS% -noverify -javaagent:"%JREBEL_HOME%\jrebel.jar" %JREBEL_PLUGINS%
)

set GRUJ_PATH="project\strap\gruj_vs_sbt-launch-0.12.4.jar"
set RUN_CMD=java %JVM_PARAMS% -jar %GRUJ_PATH% %LOG_LEVEL% %SBT_PARAMS%

:RUN_LOOP
%RUN_CMD%

if %DO_LOOP%.==true. (
  if %NO_PAUSE%.==false. (
    echo Press Enter to continue or Press CTRL+C to exit!
    pause
  )
  goto :RUN_LOOP
)

popd
endlocal
