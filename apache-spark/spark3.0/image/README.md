# DJL Spark Image Example

## Introduction

This folder contains 3 demo applications built with Spark and DJL to run image related
tasks.

- ImageClassificationExample: Ready to run for image classification using built in model from Model URL
- ObjectDetectionExample: Ready to run for object detection using built in model from Model URL
- SemanticSegmentationExample: Ready to run for semantic segmentation using built in model from Model URL

## Setup

We provide two options to build, you can choose to build with `sbt` or `gradle`.

### sbt

```
libraryDependencies += "ai.djl.spark" % "spark_2.12" % "0.27.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.27.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.27.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu-precxx11" % "2.1.1"
```

### gradle

You should add these in `dependencies`

```
dependencies {
    implementation platform("ai.djl:bom:${djl_version}")
    implementation "ai.djl.spark:spark_2.12"
    runtimeOnly "ai.djl.pytorch:pytorch-engine"
    runtimeOnly "ai.djl.pytorch:pytorch-model-zoo"
    runtimeOnly "ai.djl.pytorch:pytorch-native-cpu-precxx11"
}
```

## Run the example

Use `spark-submit` to run the examples. For example, to run the image classification example, you can run:

```
spark-submit --class com.examples.ImageClassificationExample \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.executor.memory=2G \
    --conf spark.executor.cores=2 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    build/libs/image-1.0-SNAPSHOT-all.jar
```
