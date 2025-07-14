@file:Suppress("UNCHECKED_CAST")

plugins {
    application
    id("com.gradleup.shadow") version "9.0.0-rc1"
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven ("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")

    // Use PyTorch engine
    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    // PyTorch JNI offline distribution package
    runtimeOnly("ai.djl.pytorch:pytorch-jni")

    // Uncomment one of the following native library for your target platform
    runtimeOnly("ai.djl.pytorch:pytorch-native-cpu::osx-aarch64")
    // runtimeOnly("ai.djl.pytorch:pytorch-native-cpu::linux-aarch64")
    // runtimeOnly("ai.djl.pytorch:pytorch-native-cpu::linux-x86_64")
    // runtimeOnly("ai.djl.pytorch:pytorch-native-cpu-precxx11::linux-x86_64")
    // runtimeOnly("ai.djl.pytorch:pytorch-native-cpu::linux-aarch64")
    // runtimeOnly("ai.djl.pytorch:pytorch-native-cpu::win-x86_64")

    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    implementation("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass = "com.examples.FatJar"
    }

    run.configure {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        systemProperty("file.encoding", "UTF-8")
    }

    shadowJar {
        mergeServiceFiles()
    }
}