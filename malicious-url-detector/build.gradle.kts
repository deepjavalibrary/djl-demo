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
    if ("Mac" in System.getProperty("os.name")) {
        implementation("ai.djl.pytorch:pytorch-engine")
    } else {
        implementation("ai.djl.mxnet:mxnet-engine")
    }

    implementation("commons-cli:commons-cli:${property("commons_cli_version")}")
    implementation("org.apache.httpcomponents.core5:httpcore5:${property("httpclient_version")}")
    implementation("org.apache.commons:commons-csv:${property("commons_csv_version")}")

    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass = "com.example.FilterProxy"
    }

    register<JavaExec>("train") {
        mainClass = "com.example.ModelTrainer"
        classpath = sourceSets.main.get().runtimeClasspath
    }
}
