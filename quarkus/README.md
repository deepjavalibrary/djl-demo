# Quarkus + DJL Demo

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

Note: Tested with GraalVM 20.1.0 on Mac (https://www.graalvm.org/docs/reference-manual/native-image/)
Note: Error remains with Native executable (see below)

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```
./mvnw quarkus:dev
```

Test REST API using curl command:

```bash
curl localhost:8080/detect
```

You will see following results:

```
[
	class: "n02123045 tabby, tabby cat", probability: 0.59030
	class: "n02124075 Egyptian cat", probability: 0.22663
	class: "n02123159 tiger cat", probability: 0.10025
	class: "n02127052 lynx, catamount", probability: 0.06752
	class: "n02129604 tiger, Panthera tigris", probability: 0.00907
]
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `imageclassification-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/imageclassification-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: 

```baseh
# use PyTorch engine
./mvnw clean package -Pnative -Ppytorch

# use TensorFlow engine
./mvnw clean package -Pnative -Ptensorflow
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 

```
./mvnw clean package -Pnative -Ppytorch -Dquarkus.native.container-build=true
```

You can then execute your native executable with:
 
```
target/imageclassification-1.0.0-SNAPSHOT-runner

# Turn on tensorflow javacpp debug log 
target/imageclassification-1.0.0-SNAPSHOT-runner -Dorg.bytedeco.javacpp.logger.debug=true
```

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

