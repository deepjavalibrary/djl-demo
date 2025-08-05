plugins {
    java
}
apply(file("../../../../tools/gradle/javaFormatter.gradle.kts"))

group = "org.example"
version = "1.0-SNAPSHOT"
// var djlVersion = property("djl_version") as String
// djlVersion = if (djlVersion.endsWith("-SNAPSHOT")) djlVersion else "${djlVersion}-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:0.28.0"))
    implementation("ai.djl:api")
    implementation("ai.djl.serving:serving")
}
