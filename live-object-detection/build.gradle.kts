plugins {
    java
    application
}
apply(file("../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl.pytorch:pytorch-model-zoo")
    implementation("ai.djl.opencv:opencv")

    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass = "com.examples.WebCam"
    }
}
