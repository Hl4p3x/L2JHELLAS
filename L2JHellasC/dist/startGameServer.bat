@echo off
color 9f
title L2J Hellas Game Server Console http://l2jhellas.info/
:start

java -Xmx1G -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer

REM -------------------------------------
REM If you have a big server and lots of memory,
REM you could experiment for example with:
REM java -Xmx2G -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM java -Xmx4G -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM java -Xmx6G -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM java -Xmx8G -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM -------------------------------------

if ERRORLEVEL 7 goto telldown
if ERRORLEVEL 6 goto tellrestart
if ERRORLEVEL 5 goto taskrestart
if ERRORLEVEL 4 goto taskdown
REM 3 - abort
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:taskrestart
echo Auto Task Restart ...
goto start
:restart
echo Admin Restart ...
goto start
:taskdown
echo Game Server terminated (Auto task)
goto end
:error
echo Game Server terminated abnormally
goto end
:end
echo.
echo Game Server terminated
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause