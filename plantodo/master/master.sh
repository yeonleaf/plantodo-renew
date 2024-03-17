#!/bin/bash
set -e
rm -f /var/lib/mysql/auto.cnf
mysql -u root -p1234 -e "create user 'repl'@'%' identified with mysql_native_password by '1234';"
mysql -u root -p1234 -e "grant replication slave, replication client on *.* to 'repl'@'%';"
exit;