@file:Suppress("UNCHECKED_CAST")

plugins {
    java
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"
var djlVersion = property("djl_version") as String

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${djlVersion}"))
    implementation("ai.djl:api")

    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.python:python")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:${property("log4j_slf4j_version")}")
}

tasks {
    application {
        mainClass = "com.examples.HybridPythonEngine"
    }

    run.configure {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
    }

    distTar { enabled = false }
}
