@color 17
echo OFF
CLS
@java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*; com.l2jhellas.tools.accountmanager.SQLAccountManager 2> NUL
@pause
if %errorlevel% == 0 (
echo.
echo Execution successful
echo.
) else (
echo.
echo An error has occurred while running the L2Jhellas Account Manager!
echo.
echo Possible reasons for this to happen:
echo.
echo - Missing .jar files or ../libs directory.
echo - MySQL server not running or incorrect MySQL settings:
echo    check ./config/Network/loginserver.ini
echo - Wrong data types or values out of range were provided:
echo    specify correct values for each required field
echo.
)
pause