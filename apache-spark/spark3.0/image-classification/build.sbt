name := "image-classification"

version := "0.1"

scalaVersion := "2.12.10"
scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.jcenterRepo

libraryDependencies += "ai.djl" % "api" % "0.21.0"
libraryDependencies += "ai.djl.spark" % "spark" % "0.21.0"

// libraryDependencies += "ai.djl.mxnet" % "mxnet-engine" % "0.21.0"
// libraryDependencies += "ai.djl.mxnet" % "mxnet-native-mkl" % "1.9.1"

libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.21.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.21.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu-precxx11" % "1.13.1"

// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-engine" % "0.21.0"
// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-native-cpu" % "2.7.0"
