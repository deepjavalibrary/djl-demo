#!/usr/bin/env bash

sudo rm /usr/local/cuda
sudo ln -s /usr/local/cuda-10.1 /usr/local/cuda
sudo mkdir /opt/sparkRapidsPlugin
pushd /opt/sparkRapidsPlugin || exit
sudo curl -O https://raw.githubusercontent.com/apache/spark/master/examples/src/main/scripts/getGpusResources.sh
sudo chmod 777 getGpusResources.sh
popd || exit
