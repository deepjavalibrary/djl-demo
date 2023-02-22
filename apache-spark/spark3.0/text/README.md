# DJL Spark Text Example

## Introduction
This folder contains two demo applications built with Spark 3.0 and DJL to run example text tasks.

- TextEmbeddingExample: Example to run text embedding on Spark.
- TextEncodingExample: Example to run text encoding / decoding on Spark.
- TextTokenizationExample: Example to run text tokenization on Spark.

## Setup

We provide two options to build, you can choose to build with `sbt` or `gradle`.

### sbt

```
libraryDependencies += "ai.djl.tensorflow" % "tensorflow-engine" % "0.20.0"
libraryDependencies += "ai.djl.tensorflow" % "tensorflow-native-cpu" % "2.7.0"
```

### gradle

You should add these in `dependencies`

#### TensorFlow
```
runtimeOnly "ai.djl.tensorflow:tensorflow-engine:0.20.0"
runtimeOnly "ai.djl.tensorflow:tensorflow-native-cpu:2.7.0"
```

Apart from that, you can also add `OMP_NUM_THREAD` environment variable to have the best performance optimization.
Please add this line in your `SparkConf`
```
.setExecutorEnv("OMP_NUM_THREAD", 1)
```

## Run the example

Use `spark-submit` to run the examples. For example, to run the text embedding example, you can run:

```
spark-submit --class com.examples.TextEmbeddingExample \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.executor.memory=2G \
    --conf spark.executor.cores=2 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    build/libs/text-embedding-1.0-SNAPSHOT-all.jar
```

Refer to the [Set up EMR on EKS](../image-classification-pyspark/README.md) if you want to run this example
on EMR on EKS.
