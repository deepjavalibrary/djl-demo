# DJL Example with Quarkus

This is a minimal web service using a DJL model for inference.

Under the hood, this demo uses:

- RESTEasy to expose the REST endpoints
- DJL-extension to run the example

## Requirements

To compile and run this demo you will need:

- JDK 1.8+
- DJL Quarkus Extension

To get the DJL quarkus extension, go to the extension directory at `../extension`.
Then, run `mvn package install` to install it locally.

## Building the application

Launch the Maven build on the checked out sources of this demo:

> ./mvnw install

### Live coding with Quarkus

The Maven Quarkus plugin provides a development mode that supports
live coding. To try this out:

> ./mvnw quarkus:dev

This command will leave Quarkus running in the foreground listening on port 8080.

Visit the default endpoint: [http://127.0.0.1:8080](http://127.0.0.1:8080) to view the prediction output.

### Run Quarkus in JVM mode

When you're done iterating in developer mode, you can run the application as a
conventional jar file.

First compile it:

> ./mvnw install

Then run it:

> java -jar ./target/getting-started-1.0-SNAPSHOT-runner.jar

Have a look at how fast it boots, or measure the total native memory consumption.

### Run Quarkus as a native executable

You can also create a native executable from this application without making any
source code changes. A native executable removes the dependency on the JVM:
everything needed to run the application on the target platform is included in
the executable, allowing the application to run with minimal resource overhead.

This step requires GraalVM and you must set `GRAALVM_HOME`.

See the [Building a Native Executable guide](https://quarkus.io/guides/building-native-image-guide)
+for help setting up your environment.

Compiling a native executable takes a bit longer, as GraalVM performs additional
steps to remove unnecessary codepaths. Use the  `native` profile to compile a
native executable:

> ./mvnw install -Dnative

After getting a cup of coffee, you'll be able to run this executable directly:

> ./target/getting-started-1.0-SNAPSHOT-runner
