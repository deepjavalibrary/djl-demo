name := "text"

version := "0.1"

scalaVersion := "2.12.10"
scalacOptions += "-target:jvm-1.8"

resolvers += Resolver.jcenterRepo

libraryDependencies += "ai.djl" % "api" % "0.20.0"
libraryDependencies += "ai.djl.spark" % "spark" % "0.21.0-SNAPSHOT"
