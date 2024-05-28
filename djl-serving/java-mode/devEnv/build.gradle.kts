plugins {
    java
}
apply(file("../../../tools/gradle/javaFormatter.gradle.kts"))

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

val exclusion by configurations.registering

@Suppress("UnstableApiUsage")
dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")
    exclusion("ai.djl:api:${property("djl_version")}")
}

tasks {
    jar {
        from((configurations.compileClasspath.get() - exclusion.get()).map {
            if (it.isDirectory()) it else zipTree(it)
        })
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    test {
        useJUnitPlatform()
    }
}
