@file:Suppress("UNCHECKED_CAST")

plugins {
    `java-library`
    application
}
apply(file("../../tools/gradle/javaFormatter.gradle.kts"))

group = "com.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(platform("ai.djl:bom:${property("djl_version")}"))
    implementation("ai.djl:api")
    implementation("ai.djl.huggingface:tokenizers")

    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    implementation("org.slf4j:slf4j-simple:${property("slf4j_version")}")
}

tasks {
    application {
        mainClass = System.getProperty("main", "com.examples.QuestionAnswering")
    }

    run.configure {
        standardInput = System.`in`
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        systemProperties.remove("user.dir")
    }

    sourceSets.main.get().java.files
        .filter { it.readText().contains("public static void main(String[] args)") }
        .map {
            it.path.substringAfter("java${File.separatorChar}").replace(File.separatorChar, '.')
                .substringBefore(".java")
        }
        .forEach { className ->
            val taskName = className.substringAfterLast(".")

            register<JavaExec>(name = taskName) {
                classpath = sourceSets.main.get().runtimeClasspath
                mainClass = className
                group = "application"

                allJvmArgs = allJvmArgs + listOf(
                    "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
                    "--add-opens=java.base/java.net=ALL-UNNAMED",
                    "--add-opens=java.base/java.nio=ALL-UNNAMED",
                    "--add-opens=java.base/java.time=ALL-UNNAMED",
                    "--add-opens=java.base/java.util=ALL-UNNAMED",
                    "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
                    "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
                    "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED"
                )
            }
        }
}
