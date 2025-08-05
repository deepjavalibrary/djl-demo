plugins {
    java
    application
}
apply(file("../../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"
var djlVersion = property("djl_version") as String
djlVersion = if (djlVersion.endsWith("-SNAPSHOT")) djlVersion else "${djlVersion}-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:0.28.0"))
    implementation("ai.djl:api")
    implementation("ai.djl.serving:wlm")
    implementation("org.apache.flink:flink-streaming-java:${property("flint_version")}")
    implementation("org.slf4j:slf4j-simple:${property("slf4j_version")}")

    runtimeOnly("org.apache.flink:flink-clients:${property("flint_version")}")
    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
}

tasks {
    application {
        mainClass = "com.example.SentimentAnalysis"
        applicationDefaultJvmArgs = listOf(
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.util=ALL-UNNAMED",
            "-Dorg.slf4j.simpleLogger.log.org.apache.flink=off"
        )
    }
}
