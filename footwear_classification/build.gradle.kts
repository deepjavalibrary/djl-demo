@file:Suppress("UNCHECKED_CAST")

plugins {
    java
}
apply(file("../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:model-zoo")
    implementation("ai.djl:basicdataset")
    implementation("org.slf4j:slf4j-simple:${property("slf4j_version")}")
    runtimeOnly("ai.djl.pytorch:pytorch-engine")
}

tasks {
    register<JavaExec>("training") {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = "com.examples.Training"
    }

    register<JavaExec>("inference") {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = "com.examples.Inference"
    }
}