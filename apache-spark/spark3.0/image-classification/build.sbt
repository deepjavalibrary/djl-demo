name := "image-classification"

version := "0.1"

scalaVersion := "2.12.10"
scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.jcenterRepo

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.1"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.1"
libraryDependencies += "ai.djl" % "api" % "0.12.0"

// libraryDependencies += "ai.djl.mxnet" % "mxnet-model-zoo" % "0.12.0"
// libraryDependencies += "ai.djl.mxnet" % "mxnet-native-auto" % "1.8.0"

libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.12.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-auto" % "1.8.1"

// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-model-zoo" % "0.12.0"
// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-native-auto" % "2.4.1"
