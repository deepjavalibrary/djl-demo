plugins {
    java
}
apply(file("../../../../tools/gradle/javaFormatter.gradle.kts"))

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}-SNAPSHOT"))
    implementation("ai.djl:api")
    implementation("ai.djl.serving:serving")
}
