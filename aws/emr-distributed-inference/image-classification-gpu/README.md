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
- We need a `configurations.json` file to enable NVIDIA Rapids.
```
aws emr create-cluster \
    --name "Spark cluster" \
    --release-label emr-6.2.0 \
    --region us-east-1 \
    --ebs-root-volume-size 70 \
    --applications Name=Hadoop Name=Spark \
    --ec2-attributes KeyName=myKey \
    --instance-type g3s.xlarge \
    --instance-count 3 \
    --use-default-roles \
    --configurations https://raw.githubusercontent.com/aws-samples/djl-demo/master/aws/emr-distributed-inference/image-classification-gpu/configurations.json
```

This process may take a while (approx 10 min - 15 min) to get cluster fully ready.

#### Run on the cluster

Then you can clone the repo and build the jar from the instance:

```
sudo yum install git
git clone https://github.com/aws-samples/djl-demo
cd djl-demo/aws/emr-distributed-inference/image-classification-gpu
./gradlew jar
```

After that you can submit a job by doing the followings:

```
spark-submit \
    --master yarn \
    --conf spark.executor.resource.gpu.discoveryScript=/usr/lib/spark/scripts/gpu/getGpusResources.sh \
    --conf spark.worker.resource.gpu.discoveryScript=/usr/lib/spark/scripts/gpu/getGpusResources.sh \
    --conf spark.task.resource.gpu.amount="0.5" \
    --conf spark.task.cpus=2 \
    --conf spark.executor.resource.gpu.amount=1 \
    --conf spark.worker.resource.gpu.amount=1 \
    --class com.examples.ImageClassificationExample \
    build/libs/image-classification-gpu-1.0-SNAPSHOT.jar
```
