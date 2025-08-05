plugins {
    scala
    java
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

val exclusion by configurations.registering

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("org.apache.spark:spark-sql_2.12:3.0.1")
    implementation("org.apache.spark:spark-mllib_2.12:3.0.1")
    implementation("org.apache.hadoop:hadoop-hdfs:2.7.4")
    // exclude system provided dependencies to avoid version mismatch
    exclusion("org.apache.spark:spark-sql_2.12:3.0.1")
    exclusion("org.apache.spark:spark-mllib_2.12:3.0.1")
    exclusion("org.apache.hadoop:hadoop-hdfs:2.7.4")
    implementation("ai.djl:api")

    runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
    runtimeOnly("ai.djl.pytorch:pytorch-jni")
    runtimeOnly("ai.djl.pytorch:pytorch-native-cu121")
    runtimeOnly("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    compileScala {
        scalaCompileOptions.setAdditionalParameters(listOf("-target:jvm-1.8"))
    }

    jar {
        from((configurations.compileClasspath.get() - exclusion.get()).map {
            if (it.isDirectory()) it else zipTree(it)
        })

        duplicatesStrategy = DuplicatesStrategy.WARN
        manifest {
            attributes["Main-Class"] = "com.examples.ImageClassificationExample"
        }
    }
}
