@echo off
color 9f
title L2J Hellas Login Server Console http://l2jhellas.info/
:start
REM -------------------------------------
REM Default parameters for a basic server.
java -Xmx32m -cp ./../libs/*; com.l2jhellas.loginserver.LoginServer
REM -------------------------------------

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
goto end
:error
echo LoginServer terminated abnormaly
:end
echo LoginServer terminated
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause