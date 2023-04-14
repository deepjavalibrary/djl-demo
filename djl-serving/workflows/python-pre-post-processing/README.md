# DJLServing Python Pre/Post Processing Workflow Example

This is an example of a DJLServing Workflow with Python pre/post processing.
Users can use a Python script to do pre/post processing and a model file to do prediction.

## Setup

Before starting, you need to install DJLServing. You can find installation instructions
[here](https://github.com/deepjavalibrary/djl-serving#installation).

## Define the workflow

### Step 1: Create the Python pre/post processing scripts

Currently only Java custom workflow function are supported. To use Python custom workflow function,
the main idea is to treat the Python function as a "model" and run it using the DJLServing Python engine.

In [preprocess/model.py](preprocess/model.py) we apply transformations such as resize.

In [postprocess/model.py](postprocess/model.py) we apply softmax function to convert
logits to probabilities and map to class names.

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
curl -O https://mlrepo.djl.ai/model/cv/image_classification/ai/djl/pytorch/synset.txt
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
    "preprocess": "preprocess/",
    "model": "djl://ai.djl.pytorch/resnet?translatorFactory=ai.djl.translate.NoopServingTranslatorFactory",
    "postprocess": "postprocess/"
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
{
  "tabby":0.43127909302711487,
  "tiger_cat":0.3664367198944092,
  "Egyptian_cat":0.17378516495227814,
  "tiger":0.0060998862609267235,
  "plastic_bag":0.003765482222661376
}
```
