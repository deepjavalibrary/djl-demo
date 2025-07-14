plugins {
    scala
    application
    id("com.gradleup.shadow") version "9.0.0-rc1"
    java
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")

    implementation("org.apache.spark:spark-core_2.11:2.4.5")
    implementation("org.apache.spark:spark-sql_2.11:2.4.5")

    runtimeOnly("ai.djl.aws:aws-ai")
    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.pytorch:pytorch-native-cpu::linux-x86_64")
    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    compileScala {
        scalaCompileOptions.setAdditionalParameters(listOf("-target:jvm-1.8"))
    }

    application {
        mainClass = "com.examples.DataProcessExample"
    }

    shadowJar {
        isZip64 = true
        mergeServiceFiles()
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("LICENSE*")
    }

    distTar { enabled = false }
}
