@echo off

call "%~dp0sbt.bat" ^
  client-api-logging/clean client-api-logging/publish ^
  client-api-commons/clean client-api-commons/publish ^
  client-api-cache/clean client-api-cache/publish ^
  client-api-diff/clean client-api-diff/publish ^
  client-api-params/clean client-api-params/publish ^
  client-api-transport/clean client-api-transport/publish ^
  client-api-interface/clean client-api-interface/publish ^
  client-api-core/clean client-api-core/publish ^
  ^
  client-cmdline/clean client-cmdline/publish ^
  client-gui/clean client-gui/publish ^
  ^
  client-assembly/clean client-assembly/publish ^
  client-launcher/clean client-launcher/clean ^
