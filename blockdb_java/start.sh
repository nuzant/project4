#!/bin/sh
exec java -jar target/blockchaindb-1.0.jar --id 1 &
exec java -jar target/blockchaindb-1.0.jar --id 2 &
exec java -jar target/blockchaindb-1.0.jar --id 3 &