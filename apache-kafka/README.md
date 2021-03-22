# DJL Kafka Sentiment Analysis on Twitter Data

This an example to show case how to deploy a deep learning model in [Apache  Kafka](http://kafka.apache.org/).

We will use DJL's built-in sentiment analysis model based on PyTorch to run analysis on twitter data.
There is a [sample data](data.txt) extracted from the [Kaggle Twitter Sentiment Analysis Dataset](https://www.kaggle.com/kazanova/sentiment140).
This demo will make predictions on whether the tweet is positive or negative.


## Pre-requisite

1. DJL requires JDK 8+, you can follow the [quick start guide](https://docs.djl.ai/docs/development/setup.html).
2. Kafka installed, download from [website](http://kafka.apache.org/) or use `brew install kafka` 

## Steps to run

#### 1. start zookeeper:

`zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties`

#### 2. start kafka server:

`kafka-server-start  /usr/local/etc/kafka/server.properties`

#### 3. create test topic

`kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic twitter-data`

#### 4. download data

`wget https://resources.djl.ai/demo/kafka/data.txt`

#### 5. pipe twitter data into producer

`kafka-console-producer --broker-list localhost:9092 --topic twitter-data < data.txt`

#### 6. run prediction inside consumer

`./gradlew run`

### sample output

```bash
content: @Frumph I'd hug you, too!  Poor Frumph.....
prediction: [
        class: "Positive", probability: 0.99894
        class: "Negative", probability: 0.00105
]
content: Andre Riue on neighbours..what has the world come to...internets down  lol
prediction: [
        class: "Negative", probability: 0.97309
        class: "Positive", probability: 0.02690
]
content: Looks like rain today, bet it buckets down as soon as I step outside front door, always the way !!!!, downhill all the way from today
prediction: [
        class: "Negative", probability: 0.99624
        class: "Positive", probability: 0.00375
]
```
