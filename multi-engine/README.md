# MultiEngine on DJL
This is an example of how to run multiple engines in one Java process using DJL.

Why is it good to be able to run multiple engines in one Java process?

Many deep learning frameworks have their individual strength and weaknesses when doing training/inference
on different models. Some frameworks run certain models faster and being able to switch between what engines to use 
on what models gives the benefits of being able to utilize the strengths of each engine.

This example uses an image of soccer players, puts that through a PyTorch model to do object detection to get 
a player from the image, then feeds that resulting image to an MXNet model to do pose estimation. 

## Warning
Loading multiple deep learning engines will cause OpenMP to load multiple times, which may cause a slowdown
or memory errors to occur. [Here](performance_numbers.md) are the results of a few simple benchmarks that we ran.

## Setup
Go into the multi-engine directory:

`cd multi-engine`

Run the following command to build the project:

`./gradlew build`

## Run the MultiEngine Program

Run the following command to run the project:

`./gradlew run -Dmain=com.examples.MultiEngine`

This will take the original image:

![Original Image](src/test/resources/pose_soccer.png)

Use a object detection PyTorch model to extract the player and pass the result through an 
MXNet model to do pose estimation and give us the final image with the joints marked as shown below:

![Pose Estimation](src/test/resources/joints.png)

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
Here you can change the engine version to whatever is suitable for your use case.

### Code

The main code loads the image in and calls the `detectPersonWithPyTorchModel` method where it will
load in the PyTorch model based on the filter parameters and detect a person from the image.
It will then crop the image where the person was detected and return that image.

Once we have the image of the person, we pass it to the next method `detectJointsWithMxnetModel` 
method where it loads the MxNet Model from model zoo based on the filter parameters to do joint detection 
on the image of the person. It will then output the result of the joints image to `build/output`.
