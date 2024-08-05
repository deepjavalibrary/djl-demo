plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 34
    namespace = "ai.djl.examples.objectdetection"

    defaultConfig {
        applicationId = "ai.djl.examples.objectdetection"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation("ai.djl:api") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
    implementation("ai.djl.android:core")
    runtimeOnly("ai.djl.android:onnxruntime")
    implementation("ai.djl.pytorch:pytorch-engine")
    implementation("ai.djl.android:pytorch-native")
}