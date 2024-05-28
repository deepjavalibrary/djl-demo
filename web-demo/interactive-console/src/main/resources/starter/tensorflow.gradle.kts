plugins {
    java
}

group = 'ai.djl.examples'
version = '0.0.1-SNAPSHOT'

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")
    runtimeOnly("ai.djl.tensorflow:tensorflow-model-zoo")
}
