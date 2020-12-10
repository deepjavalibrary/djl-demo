#!/usr/bin/env bash

curl -O https://archive.apache.org/dist/spark/spark-3.0.1/spark-3.0.1-bin-hadoop2.7.tgz
tar zxvf spark-3.0.1-bin-hadoop2.7.tgz
mv spark-3.0.1-bin-hadoop2.7/ spark
cp spark/conf/spark-env.sh.template spark/conf/spark-env.sh
echo 'SPARK_WORKER_OPTS="-Dspark.worker.resource.gpu.amount=1 -Dspark.worker.resource.gpu.discoveryScript=/opt/sparkRapidsPlugin/getGpusResources.sh"' >> spark/conf/spark-env.sh
