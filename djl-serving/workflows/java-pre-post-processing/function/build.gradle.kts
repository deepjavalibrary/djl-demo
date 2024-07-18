plugins {
    java
}
apply(file("../../../../tools/gradle/javaFormatter.gradle.kts"))

group = "org.example"
version = "1.0-SNAPSHOT"
var djlVersion = property("djl_version") as String
djlVersion = if (djlVersion.endsWith("-SNAPSHOT")) djlVersion else "${djlVersion}-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${djlVersion}"))
    implementation("ai.djl:api")
    implementation("ai.djl.serving:serving")
}
