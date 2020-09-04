name := "image-classification"

version := "0.1"

scalaVersion := "2.11.12"
scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.jcenterRepo

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.0"
libraryDependencies += "ai.djl" % "api" % "0.7.0"
libraryDependencies += "ai.djl" % "repository" % "0.7.0"

// libraryDependencies += "ai.djl.mxnet" % "mxnet-model-zoo" % "0.7.0"
// libraryDependencies += "ai.djl.mxnet" % "mxnet-native-auto" % "1.7.0-backport"

libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.7.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-auto" % "1.6.0"
