plugins {
    scala
    application
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl.spark:spark_2.12")
    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.huggingface:tokenizers")
    // runtimeOnly("ai.djl.pytorch:pytorch-native-cpu-precxx11")
    // runtimeOnly("ai.djl.pytorch:pytorch-jni")
}

tasks {
    compileScala {
        // scalaCompileOptions.setAdditionalParameters(["-target:jvm-1.8"])
    }

    application {
        mainClass = System.getProperty("main", "com.examples.ImageClassificationExample")
    }

    shadowJar {
        isZip64 = true
        mergeServiceFiles()
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("LICENSE*")
    }

    distTar {
        enabled = false
    }
}
