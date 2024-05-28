@file:Suppress("UNCHECKED_CAST")

plugins {
    application
    id("de.jjohannes.extra-java-module-info") version "0.11"
    id("org.beryx.jlink") version "2.26.0"
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "org.examples"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")
    implementation("ai.djl.pytorch:pytorch-model-zoo")
    implementation("ai.djl.opencv:opencv")
    implementation("org.apache.commons:commons-lang3:${property("commons_lang3_version")}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${property("log4j_slf4j_version")}")
}

tasks {
    jlink {
        forceMerge("log4j-api", "gson")
        launcher {
            name = "djl-module-demo"
        }
    }

    extraJavaModuleInfo {
        automaticModule("opencv-4.9.0-0.jar", "org.openpnp.opencv")
    }

    application {
        mainModule = "org.examples.module"
        mainClass = "org.examples.module.ModuleTest"
    }

    run.configure {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        systemProperty("file.encoding", "UTF-8")
    }

    distTar { enabled = false }
}