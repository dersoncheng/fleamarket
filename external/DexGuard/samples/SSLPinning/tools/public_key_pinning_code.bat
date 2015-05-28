@ECHO OFF

REM Tool to print out code to pin public keys in SSL.

IF EXIST "%TOOL_HOME%" GOTO home
SET TOOL_HOME=%~dp0\..
:home

java -cp "%TOOL_HOME%\dexguard_tools.jar" dexguard.tools.PublicKeyPinningTool %*
