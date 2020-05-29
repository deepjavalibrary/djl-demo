# MultiEngine on DJL
This is an example of how to run multiple engines in one Java process using DJL.

Why is it good to be able to run multiple engines in one Java process?

Many deep learning frameworks have their individual strength and weaknesses when doing training/inference
on different models. Being able to switch between what engines to use when on what models gives the benefits
of being able to utilize the strengths of each engine.

## Warning
Loading multiple deep learning engines will cause OpenMP to load multiple times, which may cause a slowdown
or memory errors to occur. [Here](performance_numbers.md) are the results of a few simple benchmarks that we ran

## Setup
Go into the multi-engine directory

`cd multi-engine`

Run the following command to build the project:

`./gradlew build`

## Run the MultiEngine Program

Run the following command to run the project:

`./gradlew run -Dmain=com.examples.MultiEngine`

This will load the Resnet50 models from the respective engine's model zoo and do inference on it with the given image.
All of this is done within one Java process.


## Configurations and the Code

### Gradle File Change for MultiEngine Dependency
To have DJL load in multiple engines, use the following dependencies:

```
def DJL_VERSION = "0.6.0-SNAPSHOT"

dependencies {
    implementation "commons-cli:commons-cli:1.4"
    implementation "org.apache.logging.log4j:log4j-slf4j-impl:2.12.1"
    implementation "ai.djl:api:${DJL_VERSION}"
    implementation "ai.djl:examples:${DJL_VERSION}"
    implementation "ai.djl:model-zoo:${DJL_VERSION}"
    implementation "ai.djl.mxnet:mxnet-model-zoo:${DJL_VERSION}"
    implementation "ai.djl.tensorflow:tensorflow-model-zoo:${DJL_VERSION}"
    implementation "ai.djl.pytorch:pytorch-model-zoo:${DJL_VERSION}"

    // See https://github.com/awslabs/djl/blob/master/mxnet/mxnet-engine/README.md for more MXNet library selection options
    runtimeOnly "ai.djl.mxnet:mxnet-native-auto:1.7.0-a-SNAPSHOT"
    runtimeOnly "ai.djl.tensorflow:tensorflow-native-auto:2.1.0-a-SNAPSHOT"
    runtimeOnly "ai.djl.pytorch:pytorch-native-auto:1.5.0-SNAPSHOT"

    testImplementation 'org.testng:testng:6.14.3'
}
```

Here you can change any of the engine version to whatever is suitable for you use case.

### Code
The `engineInference` make use of the Model Zoo and uses Criteria to search for and load models from the model zoo and
run inference on it.

The `loadingModelManuallyInference`is an example of loading custom model from different deep learning frameworks
to do inference on. Change the `path_to_model_dir` variable so DJL can load the custom model in and run it.
