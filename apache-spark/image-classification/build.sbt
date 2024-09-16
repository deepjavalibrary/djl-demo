name := "image-classification"

version := "0.1"

scalaVersion := "2.11.12"
scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.jcenterRepo

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.0"
libraryDependencies += "ai.djl" % "api" % "0.12.0"

libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.12.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-auto" % "1.8.1"
