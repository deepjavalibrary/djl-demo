@file:Suppress("UNCHECKED_CAST")

plugins {
    java
    application
}
apply(file("../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")

    runtimeOnly("ai.djl.tensorflow:tensorflow-engine")
    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass =  "com.examples.PneumoniaDetection"
    }

    run.configure {
        environment("TF_CPP_MIN_LOG_LEVEL", "1") // turn off TensorFlow print out
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        systemProperty("file.encoding", "UTF-8")
    }

    distTar {
        enabled = false
    }
}
