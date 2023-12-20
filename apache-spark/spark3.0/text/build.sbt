name := "text"

version := "0.1"

scalaVersion := "2.12.10"
scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.jcenterRepo

libraryDependencies += "ai.djl" % "api" % "0.25.0"
libraryDependencies += "ai.djl.spark" % "spark_2.12" % "0.25.0"

libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.25.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.25.0"

// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-engine" % "0.25.0"
// libraryDependencies += "ai.djl.tensorflow" % "tensorflow-native-cpu" % "2.10.1"
