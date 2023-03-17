# DJLServing Multi-Model Workflow Example

This is an example of how to serve multiple models in a DJLServing Workflow. 

This example uses an image of soccer players, puts that through an object detection model to detect
objects from the image, then define a custom workflow function to extract the person image, then feeds that
resulting image to a pose estimation model.

## Setup

Before starting, you need to install DJLServing. You can find installation instructions
[here](https://github.com/deepjavalibrary/djl-serving#installation).

## Define the workflow

First, we use the `function` folder to start building our own custom WorkflowFunction [ExtractPersonWF](function/src/main/java/org/example/ExtractPersonWF.java).
The WorkflowFunction is a lambda function that can be run within a workflow. It takes a list of inputs.
In this example, we define the ExtractPersonWF function that takes two inputs:
the input image and the detected objects. It returns the cropped person image.

Once it is done, you can package the whole project in a jar:

```
cd multi-model/function/
./gradlew jar
```

Then copy the jar in the `deps` folder under the model server home. For example, if the model server home is
`/opt/djl/`, you can copy the jar to `/opt/djl/deps/`.

```
cp build/libs/function-1.0-SNAPSHOT.jar /opt/djl/deps/
```

Once all the external definitions are defined, the actual workflow definition can be created.
We define the workflow.json to define the workflow.

```
{
  "name": "multi-model",
  "version": "0.1",
  "models": {
    "objectDetection": {
      "application": "cv/object_detection",
      "modelUrl": "djl://ai.djl.mxnet/ssd",
      "filters": {
        "backbone": "resnet50"
      }
    },
    "poseEstimation": {
      "application": "cv/pose_estimation",
      "modelUrl": "djl://ai.djl.mxnet/simple_pose",
      "filters": {
        "backbone": "resnet18",
        "dataset": "imagenet"
      }
    }
  },
  "functions": {
    "extractPerson": "org.example.ExtractPersonWF"
  },
  "workflow": {
    "detectedObjects": ["objectDetection", "in"],
    "person": ["extractPerson", "in", "detectedObjects"],
    "out": ["poseEstimation", "person"]
  }
}
```

This defines a workflow named `multi-model`. In the models section, it defines two models:
objectDetection and poseEstimation. The objectDetection model is an object detection model.
The poseEstimation model is a pose estimation model. The functions section defines the custom
function "extractPerson" that we defined in the previous step.

The workflow section defines the workflow graph. It first applies objectDetection model to the input image "in"
to find all the object in the image. Then applies extractPerson function to the input image "in" and
the result of objectDetection "detectedObjects". Finally applies poseEstimation to the cropped person image and
output to "out".

It is also possible to nest function calls by replacing arguments with a list.
That means that this operation can be defined on a single line:

```
  "workflow": {
    "out": ["poseEstimation", ["extractPerson", "in", ["objectDetection", "in"]]]
  }
```

## Run the example

### Step 1: Copy jar the deps folder

Package the project in a jar:

```
cd multi-model/function/
./gradlew jar
```

Copy the function jar in the `deps` folder under the model server home.
For example, if the model server home is `/opt/djl/`, you can copy the jar to `/opt/djl/deps/`.

```
cp build/libs/function-1.0-SNAPSHOT.jar /opt/djl/deps/
```

### Step 2: Start the model server

Start DJLServing and load the workflow at startup:

```
cd multi-model/
djl-serving -w file://$PWD/workflow.json
```

If DJLServing is already started, use the management API to register the workflow:

```
curl -X POST "http://localhost:8080/workflows?url=file://$PWD/workflow.json"
```

This will register a workflow called `multi-model`.

### Step 3: Run inference

Open another session to run inference:

```
curl -O https://resources.djl.ai/images/pose_soccer.png
curl -X POST http://localhost:8080/predictions/multi-model -T pose_soccer.png
```

This is part of the expected output:

```
{
  "joints": [
    {
      "confidence": 0.8313355445861816,
      "x": 0.2708333432674408,
      "y": 0.125
    },
    {
      "confidence": 0.8722784519195557,
      "x": 0.2916666567325592,
      "y": 0.109375
    },
    {
      "confidence": 0.809762716293335,
      "x": 0.2916666567325592,
      "y": 0.109375
    },
    ...
}
```
