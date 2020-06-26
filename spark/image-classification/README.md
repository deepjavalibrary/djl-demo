# DJL Spark Image Classification Example

## Introduction
This folder contains two demo applications built with Spark and DJL to run a group image classification
task.

- ImageClassificationExample: Ready to run for classification using built in model in ModelZoo
- DataProcessExample: If you are new to DJL and would like to learn more on the processing logic. (PyTorch Engine only)

## Setup

We provide two options to build, you can choose to build with `sbt` or `gradle`.

We use PyTorch engine by default, you can switch to MXNet or TensorFlow Engine adding these lines:

### sbt

#### MXNet
```
libraryDependencies += "ai.djl.mxnet" % "mxnet-model-zoo" % "0.6.0"
libraryDependencies += "ai.djl.mxnet" % "mxnet-native-auto" % "1.7.0-a"
```

#### TensorFlow
```
libraryDependencies += "ai.djl.tensorflow" % "tensorflow-model-zoo" % "0.6.0"
libraryDependencies += "ai.djl.tensorflow" % "tensorflow-native-auto" % "2.2.0"
```

### gradle

You should add these in `dependencies`

#### MXNet
```
runtimeOnly "ai.djl.mxnet:mxnet-model-zoo:0.6.0"
runtimeOnly "ai.djl.mxnet:mxnet-native-auto:1.7.0-a"
```

#### TensorFlow
```
runtimeOnly "ai.djl.tensorflow:tensorflow-model-zoo:0.6.0"
runtimeOnly "ai.djl.tensorflow:tensorflow-native-auto:2.2.0"
```

Apart from that, you may also need to use `NaiveEngine` mode in MXNet for multi-thread inference.
Please add this line in your `SparkConf`

```
.setExecutorEnv("MXNET_ENGINE_TYPE", "NaiveEngine")
```

## Run the example

This example will run image classification with pretrained `resnet18` model on images in the `images` folder.
The output will be saved under `out/spark_output`.

This is the expected output from console:
```
[
	class: "n02085936 Maltese dog, Maltese terrier, Maltese", probability: 0.81445
	class: "n02096437 Dandie Dinmont, Dandie Dinmont terrier", probability: 0.08678
	class: "n02098286 West Highland white terrier", probability: 0.03561
	class: "n02113624 toy poodle", probability: 0.01261
	class: "n02113712 miniature poodle", probability: 0.01200
][
	class: "n02123045 tabby, tabby cat", probability: 0.52391
	class: "n02123394 Persian cat", probability: 0.24143
	class: "n02123159 tiger cat", probability: 0.05892
	class: "n02124075 Egyptian cat", probability: 0.04563
	class: "n03942813 ping-pong ball", probability: 0.01164
][
	class: "n03770679 minivan", probability: 0.95839
	class: "n02814533 beach wagon, station wagon, wagon, estate car, beach waggon, station waggon, waggon", probability: 0.01674
	class: "n03769881 minibus", probability: 0.00610
	class: "n03594945 jeep, landrover", probability: 0.00448
	class: "n03977966 police van, police wagon, paddy wagon, patrol wagon, wagon, black Maria", probability: 0.00278
]
```
