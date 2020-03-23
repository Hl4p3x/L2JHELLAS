#!/bin/bash
err=1
until [ $err == 0 ];
do
   [ -f log/java0.log.0 ] && mv log/java0.log.0 "log/java/`date +%Y-%m-%d_%H-%M-%S`_java.log"
   [ -f log/stdout.log ] && mv log/stdout.log "log/stdout/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
   java -Dfile.encoding=UTF-8 -Xmx32m -cp ./../libs/*: com.l2jhellas.loginserver.LoginServer > log/stdout.log 2>&1
   err=$?
   sleep 10;
done