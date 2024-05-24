plugins {
    scala
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("org.scala-lang:scala-library:2.11.12")
    implementation("org.apache.spark:spark-core_2.11:2.3.3")
    implementation("ai.djl:api")

    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
}

tasks {
    compileScala {
        scalaCompileOptions.setAdditionalParameters(listOf("-target:jvm-1.8"))
    }

    application {
        mainClass = "com.examples.ImageClassificationExample"
    }

    distTar {
        enabled = false
    }

    run.configure {
        jvmArgs(
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.util=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.util.concurrent=ALL-UNNAMED"
        )
    }
}
