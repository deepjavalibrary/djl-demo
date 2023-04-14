# DJLServing Java Pre/Post Processing Workflow Example

This is an example of a DJLServing Workflow with Java pre/post processing.

This example use preprocess workflow function to preprocess an input image, puts that through a resnet model to
do inference, then use the postprocess workflow function to process the probabilities to classifications JSON output.

## Setup

Before starting, you need to install DJLServing. You can find installation instructions
[here](https://github.com/deepjavalibrary/djl-serving#installation).

## Define the workflow

### Step 1: Create the Java pre/post processing workflow functions

In [PreprocessWF.java](function/src/main/java/org/example/PreprocessWF.java) we define a custom Java workflow function
that apply transformations such as resize to the input image.

In [PostprocessWF.java](function/src/main/java/org/example/PostprocessWF.java) we define a custom Java workflow function
to process the probabilities to classifications JSON output.

Once it is done, you can package the whole project in a jar:

```
cd java-pre-post-processing/function/
./gradlew jar
```

Then copy the jar in the `deps` folder under the model server home. For example, if the model server home is
`/opt/djl/`, you can copy the jar to `/opt/djl/deps/`.

```
cp build/libs/function-1.0-SNAPSHOT.jar /opt/djl/deps/
```

### Step 2: Prepare model file

Next, we need to prepare a model file. DJL Serving supports model artifacts for the following engines:

* MXNet
* PyTorch (torchscript only)
* TensorFlow
* ONNX
* PaddlePaddle

See the documentation for more details on how to prepare the model file for
[MXNet](https://github.com/deepjavalibrary/djl/blob/master/docs/mxnet/how_to_convert_your_model_to_symbol.md),
[PyTorch](https://github.com/deepjavalibrary/djl/blob/master/docs/pytorch/how_to_convert_your_model_to_torchscript.md),
[TensorFlow](https://github.com/deepjavalibrary/djl/blob/master/docs/tensorflow/how_to_import_tensorflow_models_in_DJL.md)
and [PaddlePaddle](https://github.com/deepjavalibrary/djl/blob/master/docs/paddlepaddle/how_to_create_paddlepaddle_model.md) engine.

After we prepared the model file, we can add it to the workflow.
The workflow definition supports both local model and url.

To use a model locally, we can package model artifacts in a .zip or .tar.gz and use the file path in workflow definition.

```
mkdir resnet18 && cd resnet18
curl https://resources.djl.ai/test-models/traced_resnet18.pt -o resnet18.pt
zip resnet18.zip *
```

Or simply use an url:

```
djl://ai.djl.pytorch/resnet?translatorFactory=ai.djl.translate.NoopServingTranslatorFactory
```

Notice that we use `NoopServingTranslatorFactory` to tell DJL not to apply any default pre/post processing,
because this example wants to use its own custom pre/post processing.

### Step 3: Define the workflow

Finally, we define the workflow.json to define the workflow.

```
{
  "name": "resnet18",
  "version": "0.1",
  "models": {
    "model": "djl://ai.djl.pytorch/resnet?translatorFactory=ai.djl.translate.NoopServingTranslatorFactory"
  },
  "functions": {
    "preprocess": "org.example.PreprocessWF",
    "postprocess": "org.example.PostprocessWF"
  },
  "workflow": {
    "preprocessed": ["preprocess", "in"],
    "inferenced": ["model", "preprocessed"],
    "out": ["postprocess", "inferenced"]
  }
}
```

This defines a workflow named `resnet18`. It first applies preprocessing to the input "in".
Then applies "model" to the result of preprocessing stored in the value "preprocessed".
Finally applies postprocessing to the inferenced result output to the output "out".

## Run the example

### Step 1: Start the model server

Under the current folder, start DJLServing and load the workflow at startup:

```
djl-serving -w file://$PWD/workflow.json
```

If DJLServing is already started, use the management API to register the workflow:

```
curl -X POST "http://localhost:8080/workflows?url=file://$PWD/workflow.json"
```

This will register a workflow called `resnet18`.

### Step 2: Run inference

Open another session to run inference:

```
curl -O https://resources.djl.ai/images/kitten.jpg
curl -X POST http://localhost:8080/predictions/resnet18 -T kitten.jpg
```

This is the expected output:

```
[
  {
    "className": "n02124075 Egyptian cat",
    "probability": 0.42080432176589966
  },
  {
    "className": "n02123159 tiger cat",
    "probability": 0.26472175121307373
  },
  {
    "className": "n02123045 tabby, tabby cat",
    "probability": 0.23570497334003448
  },
  {
    "className": "n02123394 Persian cat",
    "probability": 0.04300721734762192
  },
  {
    "className": "n02127052 lynx, catamount",
    "probability": 0.006421567872166634
  }
]
```
