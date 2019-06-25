#!/bin/sh
rm -r -f /tmp/1 /tmp/2 /tmp/3
exec java -jar target/blockchaindb-1.0.jar --id=1 &
exec java -jar target/blockchaindb-1.0.jar --id=2 &
exec ./client_start.sh

sleep 100

exec java -jar target/blockchaindb-1.0.jar --id=3 &