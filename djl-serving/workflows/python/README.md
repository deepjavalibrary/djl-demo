# DJLServing Python Workflow Example

This is an example of a DJLServing Python Workflow.
User just need to prepare a python script file to run in a workflow.

## Setup

Before starting, you need to install DJLServing. You can find installation instructions
[here](https://github.com/deepjavalibrary/djl-serving#installation).

## Define the workflow

### Step 1: Define the Python model

For python model, all you need to do is to prepare a python file named `model.py` that contains the followings:

```
from djl_python import Input
from djl_python import Output


def handle(inputs: Input) -> Output:
```

In this example, we define [resnet18/model.py](https://github.com/deepjavalibrary/djl-demo/blob/master/djl-serving/workflows/python/resnet18/model.py).

### Step 2: Define the workflow

Finally, we define the workflow.json to define the workflow.

```
{
  "name": "resnet18",
  "version": "0.1",
  "models": {
    "model": "resnet18/"
  },
  "workflow": {
    "out": ["model", "in"]
  }
}
```

This defines a workflow named `resnet18`. It defines a single model. The workflow applies "model" to the input "in",
and output the inferenced result as "out".

## Run the example

### Step 1: Start the model server

Under the current folder, start DJLServing and load the workflow at startup:

```
djl-serving -m file://$PWD/workflow.json
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
    "tabby":0.4552350342273712,
    "tiger_cat":0.34835344552993774,
    "Egyptian_cat":0.15608149766921997,
    "lynx":0.02676205337047577,
    "Persian_cat":0.0022320253774523735
  }
]
```
