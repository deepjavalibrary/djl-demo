@file:Suppress("UNCHECKED_CAST")

plugins {
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")

    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.python:python")

    implementation("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass = System.getProperty("main", "com.examples.BetterSolution")
    }

    run.configure {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        // systemProperty("org.slf4j.simpleLogger.log.ai.djl.python.engine.PyProcess", "warn")
    }
}
