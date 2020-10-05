#!/usr/bin/env bash

mkdir /opt/sparkRapidsPlugin
pushd /opt/sparkRapidsPlugin || exit
curl -O https://repo1.maven.org/maven2/com/nvidia/rapids-4-spark_2.12/0.2.0/rapids-4-spark_2.12-0.2.0.jar
curl -O https://repo1.maven.org/maven2/ai/rapids/cudf/0.15/cudf-0.15-cuda10-1.jar
curl -O https://raw.githubusercontent.com/apache/spark/master/examples/src/main/scripts/getGpusResources.sh
chmod 777 getGpusResources.sh
popd || exit
