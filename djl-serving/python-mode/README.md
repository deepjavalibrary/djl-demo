# DJLServing: Python mode

## Installation

Install `djl_python` from djl-serving source code
```
git clone https://github.com/deepjavalibrary/djl-serving -b v0.17.0
cd djl-serving/engines/python/setup
pip install -U .
```

## Prepare your python file

For python model, all you need to do is to prepare a python file named `model.py` that contains the followings:

```
from djl_python import Input
from djl_python import Output


def handle(inputs: Input) -> Output:
  pass
```

And implement the handle function. DJLServing will make a call with handle to run your request.

### Add some libraries
If your python files used some pip packages that may need to install before run, you can just provide a `requirements.txt` under the same folder with your model file.

### Need to use a virtual environment
DJL will use the environment that match your current session. If you want to run python script under a certain environment, you can just run djl-serving.

There is another way is to:

```
export PYTHON_EXECUTABLE=<path-to-python3>
```
that point to your python.

## Run our example

### Step 1: Start your server

Under the current folder, just run with the endpoint, assume my endpoint name is `resnet`.


Linux/macOS

```
djl-serving -m resnet::Python=file://$PWD/resnet18
```

Windows

```
path-to-your\serving.bat -m "resnet::Python=file:///%cd%\resnet18"
```

### Step 2: Run inference

Open another sesssion to run inference

Linux/macOS

```
curl -O https://resources.djl.ai/images/kitten.jpg
curl -X POST "http://127.0.0.1:8080/predictions/resnet" -T "kitten.jpg"
```

On Windows, you can just download the image and use `Postman` to send POST request.