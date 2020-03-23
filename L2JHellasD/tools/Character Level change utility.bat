@echo off

REM ################################################
REM ## You can change here your own DB parameters ##
REM ################################################
REM MYSQL BIN PATH
set mysqlBinPath=%ProgramFiles%\MySQL\MySQL Server 5.5\bin

REM DB INFO
set gsuser=root
set gspass=
set gsdb=l2jhellas
set gshost=localhost
REM ################################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"

:ChooseChar2Lvl

echo - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
echo              You Are Running an l2jhellas ServerPack Interlude Version
echo                             Copyright 2012-2014
echo - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

echo.
echo.                              
echo List of characters:
echo -------------------
echo select char_name from characters > Chars.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < Chars.sql | more
echo -------------------
del Chars.sql
echo.
set char=0
echo Type the name of the character you want to level up.
echo You can close the window to exit.
echo.
set /p char="Player Name: "
cls

echo - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
echo              You Are Running an l2jhellas ServerPack Interlude Version
echo                             Copyright 2012-2014
echo - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

echo.
echo. 
echo Type the desire level you want for %char%
echo (Values 1-81 81=Full exp bar of 80 lvl).
if "%char%"=="0" goto ChooseChar2Lvl
set lvl=0
echo.
set /p lvl="Level: "
echo.
if "%lvl%"=="0" goto ChooseChar2Lvl
if "%lvl%"=="1" set exp=1
if "%lvl%"=="2" set exp=69
if "%lvl%"=="3" set exp=364
if "%lvl%"=="4" set exp=1169
if "%lvl%"=="5" set exp=2885
if "%lvl%"=="6" set exp=6039
if "%lvl%"=="7" set exp=11288
if "%lvl%"=="8" set exp=19424
if "%lvl%"=="9" set exp=31379
if "%lvl%"=="10" set exp=48230
if "%lvl%"=="11" set exp=71203
if "%lvl%"=="12" set exp=101678
if "%lvl%"=="13" set exp=141194
if "%lvl%"=="14" set exp=191455
if "%lvl%"=="15" set exp=254331
if "%lvl%"=="16" set exp=331868
if "%lvl%"=="17" set exp=426289
if "%lvl%"=="18" set exp=540001
if "%lvl%"=="19" set exp=675597
if "%lvl%"=="20" set exp=835863
if "%lvl%"=="21" set exp=1023785
if "%lvl%"=="22" set exp=1242547
if "%lvl%"=="23" set exp=1495544
if "%lvl%"=="24" set exp=1786380
if "%lvl%"=="25" set exp=2118877
if "%lvl%"=="26" set exp=2497078
if "%lvl%"=="27" set exp=2925251
if "%lvl%"=="28" set exp=3407898
if "%lvl%"=="29" set exp=3949755
if "%lvl%"=="30" set exp=4555797
if "%lvl%"=="31" set exp=5231247
if "%lvl%"=="32" set exp=5981577
if "%lvl%"=="33" set exp=6812514
if "%lvl%"=="34" set exp=7730045
if "%lvl%"=="35" set exp=8740423
if "%lvl%"=="36" set exp=9850167
if "%lvl%"=="37" set exp=11066073
if "%lvl%"=="38" set exp=12395216
if "%lvl%"=="39" set exp=13844952
if "%lvl%"=="40" set exp=15422930
if "%lvl%"=="41" set exp=17137088
if "%lvl%"=="42" set exp=18995666
if "%lvl%"=="43" set exp=21007204
if "%lvl%"=="44" set exp=23180551
if "%lvl%"=="45" set exp=25524869
if "%lvl%"=="46" set exp=28049636
if "%lvl%"=="47" set exp=30764655
if "%lvl%"=="48" set exp=33680053
if "%lvl%"=="49" set exp=36806290
if "%lvl%"=="50" set exp=40154163
if "%lvl%"=="51" set exp=45525134
if "%lvl%"=="52" set exp=51262491
if "%lvl%"=="53" set exp=57383989
if "%lvl%"=="54" set exp=63907912
if "%lvl%"=="55" set exp=70853090
if "%lvl%"=="56" set exp=80700832
if "%lvl%"=="57" set exp=91162655
if "%lvl%"=="58" set exp=102265882
if "%lvl%"=="59" set exp=114038596
if "%lvl%"=="60" set exp=126509653
if "%lvl%"=="61" set exp=146308200
if "%lvl%"=="62" set exp=167244337
if "%lvl%"=="63" set exp=189364894
if "%lvl%"=="64" set exp=212717908
if "%lvl%"=="65" set exp=237352644
if "%lvl%"=="66" set exp=271975263
if "%lvl%"=="67" set exp=308443198
if "%lvl%"=="68" set exp=346827154
if "%lvl%"=="69" set exp=387199547
if "%lvl%"=="70" set exp=429634523
if "%lvl%"=="71" set exp=474207979
if "%lvl%"=="72" set exp=532694979
if "%lvl%"=="73" set exp=606322775
if "%lvl%"=="74" set exp=696381369
if "%lvl%"=="75" set exp=804225364
if "%lvl%"=="76" set exp=931275364
if "%lvl%"=="77" set exp=1151275834
if "%lvl%"=="78" set exp=1511275834
if "%lvl%"=="79" set exp=2099275834
if "%lvl%"=="80" set exp=4200000000
if "%lvl%"=="81" set exp=6300000000
echo Updating database...
echo UPDATE characters SET  level='%lvl%', exp='%exp%' WHERE char_name='%char%'; > charLvl.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < charLvl.sql
echo Character level for %char% changed successfully in your database.
echo close this window if you dont want to change other levels or press any key to continue.
del charLvl.sql
pause
cls
goto ChooseChar2Lvl
