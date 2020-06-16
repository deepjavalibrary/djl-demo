# Covid19 Detection

This is an example to demonstrate how to use [Deep Java Library](http://djl.ai) to detect COVID-19 based on X-ray images.

For more details, please follow this [blog post](https://www.pyimagesearch.com/2020/03/16/detecting-covid-19-in-x-ray-images-with-keras-tensorflow-and-deep-learning/).

You can obtain the training script and a trained Keras model from the above blog post. We will use the model and DJL for prediction.

# Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./gradlew quarkusDev
```

## Packaging and running the application

The application can be packaged using `./gradlew quarkusBuild`.
It produces the `covid19-detection-quarkus-1.0-SNAPSHOT-runner.jar` file in the `build` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/lib` directory.

The application is now runnable using `java -jar build/covid19-detection-quarkus-1.0-SNAPSHOT-runner.jar`.

If you want to build an _über-jar_, just add the `--uber-jar` option to the command line:
```
./gradlew quarkusBuild --uber-jar
```

## Creating a native executable

You can create a native executable using: `./gradlew build -Dquarkus.package.type=native`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./build/covid19-detection-quarkus-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling#building-a-native-executable.

## Prepare images

You can find some X-ray images of lungs here:

* [COVID-19 infected lungs](https://github.com/ieee8023/covid-chestxray-dataset/tree/master/images)
* [Normal lungs](https://www.kaggle.com/paultimothymooney/chest-xray-pneumonia)

## Run prediction

### Maven Native (Not Yet Working)

```
mvn clean install -Dai.djl.repository.zoo.location=models/saved_model -Pnative
target/covid19-detection-quarkus-1.0.0-SNAPSHOT-runner -Dai.djl.repository.zoo.location=models/saved_model
curl http://localhost:8080/predict
```

### Gradle JVM

```
./gradlew build
java -Dai.djl.repository.zoo.location=models/saved_model -jar build/covid19-detection-quarkus-1.0-SNAPSHOT-runner.jar
curl http://localhost:8080/predict
```

### Gradle Native (Not Yet Working)

!!! Use Maven instead as it will give better GraalVM errors.

```
./gradlew build -Dquarkus.package.type=native
build/covid19-detection-quarkus-1.0-SNAPSHOT-runner -Dai.djl.repository.zoo.location=models/saved_model
curl http://localhost:8080/predict
```

# Additional Info

## Trained model

You can find a trained model [here](https://djl-tensorflow-javacpp.s3.amazonaws.com/tensorflow-models/covid-19/saved_model.zip). It is included by default in the `/model` folder.