@file:Suppress("UNCHECKED_CAST")

plugins {
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")
    implementation("ai.djl.huggingface:tokenizers")
    implementation("ai.djl.llama:llama")

    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    implementation("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass = System.getProperty("main", "com.examples.QuestionAnswering")
    }

    run.configure {
        standardInput = System.`in`
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
    }
}
