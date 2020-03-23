#! /bin/sh

# for "bad intercepter" error a solution is
# sudo apt-get install dos2unix
# and in terminal convert all sh with: dos2unix *.sh

# if Screen not found:
# sudo apt-get install screen

# if xterm not found:
# sudo apt-get install xterm

echo "try to kill all realm screens"
while
pid_realm="`ps ax | grep 'SCREEN -AdmS realm ./GameServer_loop.sh' | grep -v "grep" -m 1 | sed 's/[^0-9]?\([0-9]*\).*/\1/; s/[^0-9]*//'`"
ps -p $pid_realm > /dev/null 2>&1;
do
echo "realm = $pid_realm";
kill $pid_realm;
done

screen -AdmS realm ./GameServer_loop.sh
xterm -title 'L2JHellas Game server' -e tail -f log/stdout.log &
echo Server started.