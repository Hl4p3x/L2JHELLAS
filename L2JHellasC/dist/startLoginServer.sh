#!/bin/bash

# for "bad intercepter" error a solution is
# sudo apt-get install dos2unix
# and in terminal convert all sh with: dos2unix *.sh

# if "Screen" not found:
# sudo apt-get install screen

echo "try to kill all realm screens"
while
pid_realm=`ps ax | grep 'SCREEN -AdmS realm ./LoginServer_loop.sh' | grep -v "grep" -m 1 | sed 's/[^0-9]?\([0-9]*\).*/\1/; s/[^0-9]*//'`
ps -p $pid_realm > /dev/null 2>&1;
do
echo "realm=$pid_realm";
kill $pid_realm;
done
screen -AdmS realm ./LoginServer_loop.sh
echo start...
xterm -title 'L2JHellas Login Server' -e tail -f log/stdout.log &
