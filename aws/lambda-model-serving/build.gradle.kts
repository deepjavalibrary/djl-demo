plugins {
    `java-library`
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
    implementation("ai.djl.pytorch:pytorch-model-zoo")
    implementation("com.amazonaws:aws-lambda-java-core:${property("aws_lambda_version")}")

    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")

    testImplementation("org.testng:testng:${property("testng_version")}")
}

tasks {
    test {
        jvmArgs = listOf("--add-opens", "java.base/jdk.internal.loader=ALL-UNNAMED")

        useTestNG() {
            // suiteXmlFiles << new File(rootDir, "testng.xml") //This is how to add custom testng.xml
        }

        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed", "standardOut", "standardError")
        }
    }

    register<Zip>("buildZip") {
        dependsOn(jar)
        archiveFileName = "{$archiveBaseName}.zip"
        from(compileJava)
        from(processResources)
        into("lib") {
            from(configurations.runtimeClasspath)
        }
    }

    build {
        dependsOn("buildZip")
    }

    register<Exec>("deploy") {
        dependsOn("buildZip")
        executable("./deploy.sh")
    }
}
