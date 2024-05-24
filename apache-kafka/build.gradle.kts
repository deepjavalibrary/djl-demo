plugins {
    java
    application
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
    implementation("ai.djl:api")
    implementation("org.slf4j:slf4j-simple:${property("slf4j_version")}")
    implementation("org.apache.kafka:kafka-clients:${property("kafka_clients_version")}")

    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
}

tasks {
    application {
        mainClass = "com.example.SentimentAnalysis"
        applicationDefaultJvmArgs = listOf(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-Dorg.slf4j.simpleLogger.log.org.apache.kafka=off"
        )
    }
}