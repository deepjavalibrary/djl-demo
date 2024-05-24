#!/usr/bin/env bash

set -ex

zookeeper-server-start /opt/homebrew/etc/kafka/zookeeper.properties &
sleep 10
kafka-server-start /opt/homebrew/etc/kafka/server.properties &
sleep 5
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic twitter-data
if [[ ! -f "data.txt" ]]; then
  curl -O https://resources.djl.ai/demo/kafka/data.txt
fi
kafka-console-producer --broker-list localhost:9092 --topic twitter-data <data.txt
