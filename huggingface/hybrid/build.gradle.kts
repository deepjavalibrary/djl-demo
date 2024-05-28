@file:Suppress("UNCHECKED_CAST")

plugins {
    java
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}-SNAPSHOT"))
    implementation("ai.djl:api")

    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.python:python")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:${property("log4j_slf4j_version")}")
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
