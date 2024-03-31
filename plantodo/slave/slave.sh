#!/bin/sh
set -e
mysql -u root -p1234 -e "create user 'repl'@'%' identified with mysql_native_password by '1234';"
mysql -u root -p1234 -e "grant replication slave, replication client on *.* to 'repl'@'%';"
master_log_file=`mysql -u root -h master -P 3306 -p1234 -e "show master status\G" | grep mysql-bin`
echo ${master_log_file}
re="[a-z]*-bin.[0-9]*"
if [[ ${master_log_file} =~ $re ]];then
    master_log_file=${BASH_REMATCH[0]}
fi

master_log_pos=`mysql -uroot -h master -P 3306 -p1234 -e "show master status\G" | grep Position`
echo ${master_log_pos}
re="[0-9]+"
if [[ ${master_log_pos} =~ $re ]];then
    master_log_pos=${BASH_REMATCH[0]}
fi

stat="CHANGE MASTER TO MASTER_HOST='master',MASTER_USER='repl',MASTER_PASSWORD='1234',MASTER_LOG_FILE='${master_log_file}',MASTER_LOG_POS=${master_log_pos};"
mysql -uroot -p1234 -e "${stat}"
mysql -uroot -p1234 -e "start slave"
exit;