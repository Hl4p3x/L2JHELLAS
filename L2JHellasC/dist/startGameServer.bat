@echo off
color 9f
title L2J Hellas Game Server Console http://l2jhellas.info/
:start

REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF-8 -Xms768m -Xmx768m -Xmn256m -Xmn256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM -------------------------------------
REM If you have a big server and lots of memory,
REM you could experiment for example with:
REM
REM
REM FOR LOW COMPUTERS
REM java -Dfile.encoding=UTF-8 -Xms768m -Xmx768m -Xmn256m -Xmn256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM
REM FOR MEDIUM COMPUTERS
REM java -Dfile.encoding=UTF-8 -Xmx1536m -Xmn512m -Xmn512m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM
REM FOR GOOD COMPUTERS
REM java -Dfile.encoding=UTF-8 -Xmx2g -Xmn512m -Xmn512m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM
REM FOR SUPER COMPUTERS
REM 1: java -server -Dfile.encoding=UTF-8 -Xmx2g -Xmn512m -Xmn512m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM 2: java -server -Dfile.encoding=UTF-8 -Xmx4g -Xmn2g -Xmn1g -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts -cp ./../libs/*;l2jhellas.jar com.l2jhellas.gameserver.GameServer
REM Some of those isn't tested (we got less ram than 3Gb) ;'(
REM -------------------------------------
REM you can use
REM -Xms1024m -Xmx1024m >>>> 1GB RAM
REM -Xms2024m -Xmx2024m >>>> 2GB RAM
REM -Xms3024m -Xmx3024m >>>> 3GB RAM
REM -Xms4024m -Xmx4024m >>>> 4GB RAM
REM -Xms5024m -Xmx5024m >>>> 5GB RAM
REM -Xms6024m -Xmx6024m >>>> 6GB RAM
REM or
REM -Xms1g -Xmx1g >>>> 1GB RAM
REM -Xms2g -Xmx2g >>>> 2GB RAM
REM -Xms3g -Xmx3g >>>> 3GB RAM
REM -Xms4g -Xmx4g >>>> 4GB RAM
REM -Xms5g -Xmx5g >>>> 5GB RAM
REM -Xms6g -Xmx6g >>>> 6GB RAM
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