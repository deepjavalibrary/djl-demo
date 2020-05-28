# MultiEngine on DJL
This is an example of how to run multiple engines in one Java process using DJL.

Why is it good to be able to run multiple engines in one Java process?

Many deep learning frameworks have their individual strength and weaknesses when doing training/inference
on different models. Being able to switch between what engines to use when on what models gives the benefits
of being able to utilize the strengths of each engine.

## Warning
Loading multiple deep learning engines will cause OpenMP to load multiple times, which may cause a slowdown
or memory errors to occur. [Here](#performance-numbers) are the results of a few simple benchmarks that we ran

## Setup

Run the following command to build the project:

`./gradlew build`

## Run the MultiEngine Program

Run the following command to run the project:

`./gradlew run -Dmain=com.examples.MultiEngine`

This will load the Resnet50 models from the respective engine's model zoo and do inference on it with the given image.
All of this is done within one Java process.


## Gradle File Change for MultiEngines
To have DJL load in multiple engines, just change the build.gradle file to include the engine version snapshot
wanted. This is a change from the original if, else if, else, loads in the original master gradle file.

```
runtimeOnly "ai.djl.mxnet:mxnet-native-auto:1.7.0-a-SNAPSHOT"
runtimeOnly "ai.djl.tensorflow:tensorflow-native-auto:2.1.0-a-SNAPSHOT"
runtimeOnly "ai.djl.pytorch:pytorch-native-auto:1.5.0-SNAPSHOT"
```


## Performance Numbers

These numbers were run on a Mac Pro 2019 (2.8 Ghz Intel Core i7)

Because of the warning above, here are some benchmarking numbers to get an idea of the performance of loading in
multiple engines vs loading in only one engine.


Benchmarking Numbers for only having one engine loaded in.

|             | Engine Name | Model         | Throughput                         | Total p50  | Total p90  | inference p50 | inference p90  |
| ----------- | ----------- | ------------- | ---------------------------------- | ---------- | ---------- | ------------- | -------------- |
| 1 Engine    | MXNet       | resnet 50     | 10.19, 5000 iteration / 490602 ms. | 95.520 ms  | 103.606 ms | 90.120 ms     | 97.954 ms      |
| MultiEngine | MXNet       | resnet 50     | 10.17, 5000 iteration / 491656 ms. | 96.084 ms  | 104.198 ms | 90.696 ms     | 98.692 ms      |
|             |             |               |                                    |            |            |               |                |
| 1 Engine    | TensorFlow  | resnet 50     | 7.15, 2500 iteration / 349777 ms.  | 123.493 ms | 142.029 ms | 110.592 ms    | 126.268 ms     |
| MultiEngine | TensorFlow  | resnet 50     | 6.84, 2500 iteration / 365355 ms.  | 124.743 ms | 160.793 ms | 110.935 ms    | 144.142 ms     |
|             |             |               |                                    |            |            |               |                |
| 1 Engine    | PyTorch     | resnet 50     | 6.87, 5000 iteration / 728242 ms.  | 140.246 ms | 155.400 ms | 133.463 ms    | 147.721 ms     |
| MultiEngine | PyTorch     | resnet 50     | 6.59, 5000 iteration / 759261 ms.  | 144.358 ms | 168.535 ms | 137.336 ms    | 160.346 ms     |
|             |             |               |                                    |            |            |               |                |
| 1 Engine    | MXNet       | resnet 18     | 22.04, 5000 iteration / 226893 ms. | 43.487 ms  | 52.077 ms  | 38.105 ms     | 45.660 ms      |
| MultiEngine | MXNet       | resnet 18     | 22.64, 5000 iteration / 220807 ms. | 42.750 ms  | 47.923 ms  | 37.482 ms     | P90: 42.109 ms |
|             |             |               |                                    |            |            |               |                |
|             |             |               |                                    |            |            |               |                |
| 1 Engine    | Tensorflow  | MobileNet 1.0 | 30.45, 5000 iteration / 164225 ms. | 31.190 ms  | 34.780 ms  | 19.983 ms     | 22.836 ms      |
| MultiEngine | Tensorflow  | MobileNet 1.0 | 29.17, 5000 iteration / 171395 ms. | 31.860 ms  | 38.047 ms  | 20.631 ms     | 24.798 ms      |
|             |             |               |                                    |            |            |               |                |
| 1 Engine    | PyTorch     | resnet 18     | 14.90, 5000 iteration / 335509 ms. | 64.268 ms  | 73.352 ms  | 57.620 ms     | 65.733 ms      |
| MultiEngine | PyTorch     | resnet 18     | 13.54, 5000 iteration / 369255 ms. | 71.959 ms  | 81.688 ms  | 64.580 ms     | 73.439 ms      |













