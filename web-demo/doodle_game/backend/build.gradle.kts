plugins {
    java
}
apply(file("../../../tools/gradle/javaFormatter.gradle.kts"))

group = "ai.djl.examples.doodle"
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
    implementation("com.amazonaws:aws-lambda-java-core:${property("aws_lambda_version")}")

    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    register<Zip>("buildZip") {
        dependsOn(jar)
        archiveFileName = "${archiveBaseName}.zip"
        from(compileJava)
        from(processResources)
        into("lib") {
            from(configurations.runtimeClasspath)
        }
    }

    build{
        dependsOn("buildZip")
    }
}
