@file:Suppress("UNCHECKED_CAST")

plugins {
    java
    application
}
apply(file("../tools/gradle/javaFormatter.gradle.kts"))

group = "com.example"
version = "1.0-SNAPSHOT"

val djlVersion = getEnv("DJL_VERSION", "0.30.0")
val engine: String = getEnv("DJL_ENGINE", "pytorch-native-auto")
val os = getOsName()
val arch: String = if (System.getProperty("os.arch") == "amd64") "x86_64" else System.getProperty("os.arch")
val stagingRepo = getEnv("DJL_STAGING", "")
val ptVersion = getEnv("PT_VERSION", "2.3.1")
val ptJniVersion = ptVersion.replace("-SNAPSHOT", "")

repositories {
    mavenCentral()
    mavenLocal()
    if (stagingRepo.isNotEmpty()) {
        for (repo in stagingRepo.split(",")) {
            maven("https://oss.sonatype.org/service/local/repositories/${repo}/content/")
        }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${djlVersion}"))
    implementation("ai.djl.sentencepiece:sentencepiece")
    implementation("ai.djl.huggingface:tokenizers")

    implementation("commons-cli:commons-cli:${property("commons_cli_version")}")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:${property("log4j_slf4j_version")}")

    if (engine.startsWith("pytorch")) {
        runtimeOnly("ai.djl.pytorch:pytorch-model-zoo")
        if (!engine.contains("-auto")) {
            runtimeOnly("ai.djl.pytorch:pytorch-jni:${ptJniVersion}-${djlVersion}!!")
            runtimeOnly("ai.djl.pytorch:${engine}:${ptVersion}!!:${os}-${arch}")
        }
        runtimeOnly("ai.djl:basicdataset")
        runtimeOnly("ai.djl:model-zoo")
        runtimeOnly("ai.djl.aws:aws-ai")
        runtimeOnly("ai.djl.hadoop:hadoop")
    } else if (engine.startsWith("tensorflow")) {
        runtimeOnly("ai.djl.tensorflow:tensorflow-model-zoo")
        if (!engine.contains("-auto")) {
            runtimeOnly("ai.djl.tensorflow:${engine}::${os}-${arch}")
        }
    } else if (engine.startsWith("onnxruntime")) {
        runtimeOnly("ai.djl.onnxruntime:onnxruntime-engine")
        runtimeOnly("ai.djl.pytorch:pytorch-engine")
    } else if (engine.startsWith("fasttext")) {
        runtimeOnly("ai.djl.fasttext:fasttext-engine")
        runtimeOnly("ai.djl.pytorch:pytorch-engine")
    } else if (engine.startsWith("xgboost")) {
        if (engine == "xgboost-gpu") {
            runtimeOnly("ai.djl.ml.xgboost:xgboost-gpu")
            runtimeOnly("ai.rapids:cudf:${property("rapis_version")}:cuda11")
        } else {
            runtimeOnly("ai.djl.ml.xgboost:xgboost")
        }
    } else if (engine.startsWith("lightgbm")) {
        runtimeOnly("ai.djl.ml.lightgbm:lightgbm")
    } else if (engine.startsWith("tensorrt")) {
        runtimeOnly("ai.djl.tensorrt:tensorrt")
        runtimeOnly("ai.djl.pytorch:pytorch-engine")
    } else if (engine.startsWith("python")) {
        runtimeOnly("ai.djl.python:python")
    } else if (engine.startsWith("tokenizers")) {
        runtimeOnly("ai.djl.pytorch:pytorch-engine")
    } else if (engine.startsWith("sentencepiece")) {
        runtimeOnly("ai.djl.pytorch:pytorch-engine")
    } else if (engine.startsWith("mxnet")) {
        runtimeOnly("ai.djl.mxnet:mxnet-model-zoo")
        if (!engine.contains("-auto")) {
            runtimeOnly("ai.djl.mxnet:${engine}::${os}-${arch}")
        }
    } else {
        throw GradleException("Unsupported engine: ${engine}.")
    }
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    application {
        mainClass = "ai.djl.canary.CanaryTest"
    }

    run.configure {
        environment("TF_CPP_MIN_LOG_LEVEL", "1") // turn off TensorFlow print out
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
        systemProperty("disableProgressBar", "true")
    }

    distTar { enabled = false }
}

fun getOsName(): String {
    val osName = System.getProperty("os.name")
    return if (osName.startsWith("Win")) {
        "win"
    } else if (osName.startsWith("Mac")) {
        "osx"
    } else if (osName.startsWith("Linux")) {
        "linux"
    } else {
        throw GradleException("Unsupported os: $osName")
    }
}

fun getEnv(key: String, defaultValue: String): String {
    val value = System.getenv(key)
    return if (value == null || value.isEmpty()) defaultValue else value
}
