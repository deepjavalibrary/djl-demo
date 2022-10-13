organization := "com.examples"
name := "fatjar"
version := "1.0-SNAPSHOT"

crossPaths := false
autoScalaLibrary := false

resolvers += Resolver.mavenCentral

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.36"
libraryDependencies += "ai.djl" % "api" % "0.19.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.19.0"

// PyTorch JNI offline distribution package
libraryDependencies += "ai.djl.pytorch" % "pytorch-jni" % "0.19.0"

// Uncomment one of the following native library for your target platform
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "1.12.1" classifier "osx-x86_64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "1.12.1" classifier "osx-aarch64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "1.12.1" classifier "linux-x86_64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "1.12.1" classifier "linux-aarch64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu-precxx11" % "1.12.1" classifier "linux-x86_64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "1.12.1" classifier "win-x86_64"

libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.19.0"
