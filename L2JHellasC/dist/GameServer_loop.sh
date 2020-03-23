#!/bin/bash

# if any problems about bad intercepter a solution is
# sudo apt-get install dos2unix
# and in terminal convert all sh with: dos2unix *.sh

err=1
until [ $err == 0 ];
do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/java/`date +%Y-%m-%d_%H-%M-%S`_java.log.0"
	[ -f log/stdout.log ] && mv log/stdout.log "log/stdout/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	
	# For developers mostly (1. line gc logrotate, 2. line parameters for gc logging):
	#[ -f log/gc.log ] && mv log/gc.log "log/gc/`date +%Y-%m-%d_%H-%M-%S`_gc.log" -verbose:gc -Xloggc:log/gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution
	java -Dfile.encoding=UTF-8 -Xmx768m -Xmn256m -Xmn256m -XX:PermSize=32m -cp ./../libs/*: com.l2jhellas.gameserver.GameServer > log/stdout.log 2>&1
	err=$?
	sleep 10;
done