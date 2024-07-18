plugins {
    java
}

group = "com.example"
version = "1.0-SNAPSHOT"

val djlVersion = getEnv("DJL_VERSION", "${property("djl_version")}")
val stagingRepo = getEnv("DJL_STAGING", "")

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
    implementation("ai.djl:api")
    implementation("ai.djl:basicdataset")
    implementation("ai.djl:model-zoo")
    implementation("ai.djl.audio:audio")
    implementation("ai.djl.aws:aws-ai")
    implementation("ai.djl.fasttext:fasttext-engine")
    implementation("ai.djl.hadoop:hadoop")
    implementation("ai.djl.huggingface:tokenizers")
    implementation("ai.djl.ml.lightgbm:lightgbm")
    implementation("ai.djl.ml.xgboost:xgboost-gpu")
    implementation("ai.djl.ml.xgboost:xgboost")
    implementation("ai.djl.mxnet:mxnet-engine")
    implementation("ai.djl.mxnet:mxnet-model-zoo")
    implementation("ai.djl.mxnet:mxnet-native-cu112mkl::linux-x86_64")
    implementation("ai.djl.mxnet:mxnet-native-mkl::linux-x86_64")
    implementation("ai.djl.mxnet:mxnet-native-mkl::osx-x86_64")
    implementation("ai.djl.mxnet:mxnet-native-mkl::win-x86_64")
    implementation("ai.djl.onnxruntime:onnxruntime-engine")
    implementation("ai.djl.opencv:opencv")
    implementation("ai.djl.pytorch:pytorch-engine")
    implementation("ai.djl.pytorch:pytorch-jni")
    implementation("ai.djl.pytorch:pytorch-model-zoo")
    implementation("ai.djl.pytorch:pytorch-native-cpu-precxx11::linux-aarch64")
    implementation("ai.djl.pytorch:pytorch-native-cpu-precxx11::linux-x86_64")
    implementation("ai.djl.pytorch:pytorch-native-cpu::linux-x86_64")
    implementation("ai.djl.pytorch:pytorch-native-cpu::osx-aarch64")
    implementation("ai.djl.pytorch:pytorch-native-cpu::osx-x86_64")
    implementation("ai.djl.pytorch:pytorch-native-cpu::win-x86_64")
    implementation("ai.djl.pytorch:pytorch-native-cu121-precxx11::linux-x86_64")
    implementation("ai.djl.pytorch:pytorch-native-cu121::linux-x86_64")
    implementation("ai.djl.pytorch:pytorch-native-cu121::win-x86_64")
    implementation("ai.djl.sentencepiece:sentencepiece")
    implementation("ai.djl.spark:spark_2.12")
    implementation("ai.djl.tablesaw:tablesaw")
    implementation("ai.djl.tensorflow:tensorflow-api")
    implementation("ai.djl.tensorflow:tensorflow-engine")
    implementation("ai.djl.tensorflow:tensorflow-model-zoo")
    implementation("ai.djl.tensorflow:tensorflow-native-cpu::linux-x86_64")
    implementation("ai.djl.tensorflow:tensorflow-native-cpu::osx-x86_64")
    implementation("ai.djl.tensorflow:tensorflow-native-cpu::win-x86_64")
    implementation("ai.djl.tensorflow:tensorflow-native-cu121::linux-x86_64")
    implementation("ai.djl.tensorflow:tensorflow-native-cu121::win-x86_64")
    implementation("ai.djl.tensorrt:tensorrt")
    implementation("ai.djl.timeseries:timeseries")
    implementation("com.microsoft.onnxruntime:onnxruntime")
    implementation("com.microsoft.onnxruntime:onnxruntime_gpu")

    if (System.getenv("FULL_RELEASE") != null) {
        implementation("ai.djl:serving")
        implementation("ai.djl.android:core")
        implementation("ai.djl.android:onnxruntime")
        implementation("ai.djl.android:pytorch-native")
        implementation("ai.djl.python:python")
    }
}

fun getEnv(key: String, defaultValue: String): String {
    val value = System.getenv(key)
    return if (value == null || value.isEmpty()) defaultValue else value
}
