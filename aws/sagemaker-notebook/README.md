# Use DJL Notebook on SageMaker Notebook

Amazon SageMaker Notebook is a all-you-can host Notebook instance with support for different ML tasks. Currently DJL contains Scala and Java notebooks that can be easily run on SageMaker Notebook. Please follow the instructions below to setup. You can choose between "Setup Java" and "Setup Scala" to setup for the corresponding environment.

## Setup Java

Let's start by spawn a terminal in JupyterLab.

We will use [IJava](https://github.com/SpencerPark/IJava) Kernel for Java API. You can run the following command to install it.

```bash
curl -L https://github.com/SpencerPark/IJava/releases/download/v1.3.0/ijava-1.3.0.zip -o ijava-kernel.zip &> /dev/null
unzip -q ijava-kernel.zip -d ijava-kernel && cd ijava-kernel && python3 install.py --sys-prefix &> /dev/null
```

Wait for approximately 20 second and refresh the JupyterLab. You will see the Java kernel available on the right-hand side.

Now, you can try with different Notebooks provided by DJL:

- [DJL Jupyter Notebooks](https://github.com/awslabs/djl/tree/master/jupyter)
- [Dive into Deep Learning (DJL edition)](https://github.com/aws-samples/d2l-java)

## Setup Scala

Let's start by spawn a terminal in JupyterLab.

We need to install the [Almond](https://almond.sh/)(Scala Jupyter Kernel). You can run the following command to install it:

```bash
curl -Lo coursier https://git.io/coursier-cli && chmod +x coursier
./coursier launch --fork almond --scala 2.12 -- --install && rm -f coursier
```

Wait for approximately 20 second and refresh the JupyterLab. You will see the Scala kernel available on the right-hand side.

### Install Spark (Optional)

Some applications require you to have standalone setup for Spark. You can use the following command in terminal to install Spark:

```bash
curl -O https://archive.apache.org/dist/spark/spark-3.0.0/spark-3.0.0-bin-hadoop2.7.tgz
tar zxvf spark-3.0.0-bin-hadoop2.7.tgz
mv spark-3.0.0-bin-hadoop2.7/ spark
```

If you need GPU support for Spark, you can run the following command to set it up:

```bash
mkdir /opt/sparkRapidsPlugin
pushd /opt/sparkRapidsPlugin || exit
curl -O https://raw.githubusercontent.com/apache/spark/master/examples/src/main/scripts/getGpusResources.sh
chmod 777 getGpusResources.sh
popd || exit
cp spark/conf/spark-env.sh.template spark/conf/spark-env.sh
echo 'SPARK_WORKER_OPTS="-Dspark.worker.resource.gpu.amount=1 -Dspark.worker.resource.gpu.discoveryScript=/opt/sparkRapidsPlugin/getGpusResources.sh"' >> spark/conf/spark-env.sh
```

We can start our standalone cluster by doing the followings:

```bash
export SPARK_MASTER_HOST=localhost
export SPARK_WORKER_INSTANCES=1
./spark/sbin/start-master.sh
./spark/sbin/start-slave.sh spark://localhost:7077
```

If there is anytime you need to restart your cluster with different setup, you can stop them and reuse the above script to start again.

```bash
./spark/sbin/stop-master.sh
./spark/sbin/stop-slave.sh
```


