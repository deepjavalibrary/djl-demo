plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 34
    namespace = "ai.djl.examples.speechrecognition"

    defaultConfig {
        applicationId = "ai.djl.examples.speechrecognition"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation("ai.djl:api") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
    implementation("ai.djl.android:core")
    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    runtimeOnly("ai.djl.android:pytorch-native")
    runtimeOnly("ai.djl.audio:audio")
    runtimeOnly("org.bytedeco:ffmpeg:6.1.1-1.5.10:android-arm64")
    runtimeOnly("org.bytedeco:ffmpeg:6.1.1-1.5.10:android-x86_64")
}

configurations.implementation {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
}
