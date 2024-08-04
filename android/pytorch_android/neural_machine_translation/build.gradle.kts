plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 34
    namespace = "ai.djl.examples.neuralmachinetranslation"

    defaultConfig {
        applicationId = "ai.djl.example.neuralmachinetranslation"
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
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("ai.djl:api") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }

    implementation("ai.djl.android:core")
    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    runtimeOnly("ai.djl.android:pytorch-native")
}

configurations.implementation {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
}
