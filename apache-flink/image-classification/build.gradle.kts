plugins {
    java
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation(platform("software.amazon.awssdk:bom:${property("awssdk_version")}"))
    implementation("ai.djl:api")
    implementation("org.apache.flink:flink-streaming-java:${property("flint_version")}")
    implementation("software.amazon.awssdk:s3")

    runtimeOnly("org.apache.flink:flink-clients:${property("flint_version")}")
    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.huggingface:tokenizers")
    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass = "com.example.EMI"
        applicationDefaultJvmArgs = listOf(
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.util=ALL-UNNAMED",
            "--add-opens",
            "java.desktop/java.awt=ALL-UNNAMED",
            "--add-opens",
            "java.desktop/java.awt.color=ALL-UNNAMED",
            "--add-opens",
            "java.desktop/java.awt.image=ALL-UNNAMED",
            "--add-opens",
            "java.desktop/sun.awt.image=ALL-UNNAMED",
            "--add-opens",
            "java.desktop/sun.java2d=ALL-UNNAMED",
            "-Dorg.slf4j.simpleLogger.log.org.apache.flink=off"
        )
    }
}
