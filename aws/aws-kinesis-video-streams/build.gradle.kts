@file:Suppress("UNCHECKED_CAST")

plugins {
    java
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

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
    implementation("commons-cli:commons-cli:${property("commons_cli_version")}")
    implementation("software.amazon.awssdk:kinesisvideo:${property("awssdk_version")}")
    implementation("software.amazon.kinesis:amazon-kinesis-client:${property("kinesis_client_version")}")
    implementation("com.amazonaws:amazon-kinesis-video-streams-parser-library:${property("kinesis_video_stream_parser_version")}")

    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:${property("log4j_slf4j_version")}")
}

tasks {
    application {
        mainClass = "com.examples.App"
    }

    run.configure {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        systemProperty("file.encoding", "UTF-8")
    }
}
