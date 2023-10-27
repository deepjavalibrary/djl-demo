# DJL Spark Audio Example

## Introduction

This folder contains 2 demo applications built with Spark and DJL to run audio related
tasks.

- SpeechRecognitionExample: Ready to run for speech recognition using Wav2Vec2 model

## Setup

We provide two options to build, you can choose to build with `sbt` or `gradle`.

### sbt

```
libraryDependencies += "ai.djl.spark" % "spark_2.12" % "0.24.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.24.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.24.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu-precxx11" % "1.13.1"
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

Use `spark-submit` to run the examples. For example, to run the speech recognition example, you can run:

```
spark-submit --class com.examples.SpeechRecognitionExample \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.executor.memory=2G \
    --conf spark.executor.cores=2 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    build/libs/audio-1.0-SNAPSHOT-all.jar
```
