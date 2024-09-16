organization := "com.examples"
name := "fatjar"
version := "1.0-SNAPSHOT"

crossPaths := false
autoScalaLibrary := false

resolvers += Resolver.mavenCentral

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.36"
libraryDependencies += "ai.djl" % "api" % "0.30.0"
libraryDependencies += "ai.djl.pytorch" % "pytorch-engine" % "0.30.0"

// PyTorch JNI offline distribution package
libraryDependencies += "ai.djl.pytorch" % "pytorch-jni" % "2.4.0-0.30.0"

// Uncomment one of the following native library for your target platform
libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "2.4.0" classifier "osx-x86_64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "2.4.0" classifier "osx-aarch64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "2.4.0" classifier "linux-x86_64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "2.4.0" classifier "linux-aarch64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu-precxx11" % "2.4.0" classifier "linux-x86_64"
// libraryDependencies += "ai.djl.pytorch" % "pytorch-native-cpu" % "2.4.0" classifier "win-x86_64"

libraryDependencies += "ai.djl.pytorch" % "pytorch-model-zoo" % "0.30.0"
