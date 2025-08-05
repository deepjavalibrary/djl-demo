plugins {
    java
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")
    implementation("ai.djl.pytorch:pytorch-engine")
}

tasks {
    application {
        mainClass = "ai.djl.examples.serving.javaclient.DJLServingClientExample1"
    }
}
