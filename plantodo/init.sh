#!/bin/sh
docker compose --profile=con up -d
sleep 30s
winpty docker exec -it master bash -c "\/root/master.sh && exit;"
winpty docker exec -it slave bash -c "\/root/slave.sh && exit;"
winpty docker exec -it master bash -c "mysql -u root -p1234 < /data.sql"
exit;
