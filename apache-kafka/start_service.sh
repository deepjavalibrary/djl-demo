zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
kafka-server-start  /usr/local/etc/kafka/server.properties
kafka-topics --zookeeper localhost:2181 --delete --topic twitter-data
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic twitter-data
kafka-console-producer --broker-list localhost:9092 --topic twitter-data < data.txt