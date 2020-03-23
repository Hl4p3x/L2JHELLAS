#!/bin/sh

# for "bad intercepter" error a solution is
# sudo apt-get install dos2unix
# and in terminal convert all sh with: dos2unix *.sh

java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*: com.l2jhellas.tools.accountmanager.SQLAccountManager
