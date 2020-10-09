# DJL Spark GPU Image Classification Example

## Introduction
This folder contains image classification applications built with Spark 3.0 and DJL to run a group image classification task.

By default, this example will only work on the machine with GPU and CUDA 10.1 built-in.
If you are looking for some other CUDA version support, please change 

```
    runtimeOnly "ai.djl.pytorch:pytorch-native-cu101::linux-x86_64"
```
in the dependency to match with.

## Setup

### AWS EMR Yarn

#### Create EMR Cluster

Currently, EMR support Spark 3.0 and GPU instances. We can create a GPU cluster using AWS CLI:

- To run this successfully, you need to change `myKey` to your EC2 pem key name.
- You can also remove the region if you have that preconfigured in your AWS CLI.
- We used the Deep Learning base AMI as a default to leverage GPU driver.

```
aws emr create-cluster \
    --name "Spark cluster" \
    --release-label emr-6.1.0 \
    --region us-east-1 \
    --custom-ami-id ami-062a33acbbfd29c6f \
    --ebs-root-volume-size 70 \
    --applications Name=Hadoop Name=Spark \
    --ec2-attributes KeyName=myKey \
    --instance-type g3s.xlarge \
    --instance-count 3 \
    --use-default-roles \
    --bootstrap-actions Path=s3://alpha-djl-demos/spark-demo/emr-prepare.sh
```

This process may take a while (approx 10 min - 15 min) to get cluster fully ready.

#### Run on the cluster

Then you can clone the repo and build the jar from the instance:

```
git clone https://github.com/aws-samples/djl-demo
cd djl-demo/aws/emr-distributed-inference/image-classification-gpu
./gradlew jar
```

After that you can submit a job by doing the followings:

```
spark-submit \
    --master yarn \
    --conf spark.executor.resource.gpu.discoveryScript=/opt/sparkRapidsPlugin/getGpusResources.sh \
    --conf spark.worker.resource.gpu.discoveryScript=/opt/sparkRapidsPlugin/getGpusResources.sh \
    --conf spark.task.resource.gpu.amount="0.5" \
    --conf spark.task.cpus=2 \
    --conf spark.executor.resource.gpu.amount=1 \
    --conf spark.worker.resource.gpu.amount=1 \
    --class com.examples.ImageClassificationExample \
    build/libs/image-classification-gpu-1.0-SNAPSHOT.jar
```

### Standalone

This application requires Java 8+ to execute.

#### Switch to CUDA 10.1

You can grab a GPU EC2 instance and use Deep Learning Base AMI as the OS. Then you can switch the system default CUDA version by following [instruction](https://docs.aws.amazon.com/dlami/latest/devguide/tutorial-base.html)

```
sudo rm /usr/local/cuda
sudo ln -s /usr/local/cuda-10.1 /usr/local/cuda
```

After that, you can verify the version by typing:

```
nvcc --version
```

#### Install Spark Rapids library

Install CUDA dependencies by following the [NVIDIA provided steps](https://nvidia.github.io/spark-rapids/docs/get-started/getting-started-on-prem.html#spark-standalone-cluster).

For your convenience, we provide some quick setup for your local machine:

```
sudo bash ./install_rapid.sh
```

#### Install Spark

After this step, we can start installing Spark 3.0 on your machine and configure worker :

```
./install_spark.sh
```

#### Launch Spark machine

Then, before we launch the machine, we can do the following configuration:
```
export SPARK_RAPIDS_DIR=/opt/sparkRapidsPlugin
export SPARK_CUDF_JAR=${SPARK_RAPIDS_DIR}/cudf-0.15-cuda10-1.jar
export SPARK_RAPIDS_PLUGIN_JAR=${SPARK_RAPIDS_DIR}/rapids-4-spark_2.12-0.2.0.jar
export SPARK_MASTER_HOST=localhost
export SPARK_WORKER_INSTANCES=1
```

And do the following to start a spark machine

```
./spark/sbin/start-master.sh
./spark/sbin/start-slave.sh spark://localhost:7077
```

#### Start execution

Let's prepare for the jar file
```
./gradlew clean jar
```

To submit a spark job:

```
./spark/bin/spark-submit \
    --master spark://localhost:7077 \
    --conf spark.task.resource.gpu.amount="0.25" \
    --conf spark.task.cpus=2 \
    --conf spark.executor.resource.gpu.amount=1 \
    --class com.examples.ImageClassificationExample \
    build/libs/image-classification-gpu-1.0-SNAPSHOT.jar
```

Here we set Task GPU to 0.25 meaning we will share 1 GPU with 4 tasks.

You may need to change the `spark.task.cpus` number to make it match with GPU sharing.

#### Clean up

```
./spark/sbin/stop-master.sh
./spark/sbin/stop-slave.sh
```
