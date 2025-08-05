@file:Suppress("UNCHECKED_CAST")

plugins {
    java
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")

    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:${property("log4j_slf4j_version")}")
}

tasks {
    application {
        mainClass = "com.example.inferentia.InferentiaDemo"
        applicationDefaultJvmArgs = listOf("-Xss2m")
    }

    run.configure {
        environment("PYTORCH_PRECXX11", "true") // must use precxx11 libtorch
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
    }

    register<JavaExec>("benchmark") {
        environment("PYTORCH_PRECXX11", "true") // must use precxx11 libtorch
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = "com.example.inferentia.Benchmark"
    }

    distTar { enabled = false }
}

