plugins {
    id("org.springframework.boot") version "4.0.0-M2"
    java
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

apply(plugin = "io.spring.dependency-management")

group = "ai.djl.examples"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.apache.commons:commons-compress:${property("commons_compress_version")}")
    implementation("javax.servlet:javax.servlet-api:${property("servlet_version")}")

    runtimeOnly("ai.djl:api")
    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.mxnet:mxnet-model-zoo")
    runtimeOnly("ai.djl.tensorflow:tensorflow-model-zoo")
}

