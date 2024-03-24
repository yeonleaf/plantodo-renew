#!/bin/bash
set -e
rm -f /var/lib/mysql/auto.cnf
mysql -u root -h 172.19.0.11 -p1234 -e "create user 'repl'@'%' identified with mysql_native_password by '1234';"
mysql -u root -h 172.19.0.11 -p1234 -e "grant replication slave, replication client on *.* to 'repl'@'%';"
sleep 20
mysql -u root -h 172.19.0.11 -p1234 < /data.sql
/bin/bash
exit;