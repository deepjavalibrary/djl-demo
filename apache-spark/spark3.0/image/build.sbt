name := "image"

version := "0.1"

scalaVersion := "2.12.10"
scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.jcenterRepo

libraryDependencies += "ai.djl.spark" % "spark_2.12" % "0.30.0"

libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.30.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.30.0"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu-precxx11" % "2.4.0"

// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-engine" % "0.30.0"
// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-native-cpu" % "2.16.1"
