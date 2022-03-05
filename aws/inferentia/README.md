## Low cost inference with AWS Inferentia

[AWS Inferentia](https://aws.amazon.com/machine-learning/inferentia/) is a high performance machine
learning inference chip, custom designed by AWS. Amazon EC2 Inf1 instances are powered by AWS
Inferentia chips, which provides you with the lowest cost per inference in the cloud and lower
the barriers for everyday developers to use machine learning (ML) at scale. Customers using models
such as YOLO v3 and YOLO v4 can get up to 1.85 times higher throughput and up to 40% lower cost per
inference compared to the EC2 G4 GPU-based instances.

In the demo, you will learn how to run PyTorch model with DJL on Amazon EC2 Inf1 instances.

## Setup environment

### Launch Inf1 EC2 instance

Please launch Inf1 instance by following the [Install Neuron Instructions](https://awsdocs-neuron.readthedocs-hosted.com/en/latest/neuron-intro/pytorch-setup/pytorch-install.html#install-neuron-pytorch)

This demo tested on Neuron SDK 1.16.1 and PyTorch 1.10.1 on Ubuntu DLAMI.
Please make sure you have Neuron Runtime 2.x installed:

```
sudo apt-get update -y

# - Stop any existing Neuron runtime 1.0 daemon (neuron-rtd) by calling
sudo systemctl stop neuron-rtd

sudo apt-get install linux-headers-$(uname -r) -y
sudo apt-get install aws-neuron-dkms -y
sudo apt-get install aws-neuron-tools -y
```

### Install AWS Inferentia neuron SDK

The Inferentia neuron SDK is required for converting PyTorch pre-trained model into neuron traced model.

```
python3 -m venv myenv

source myenv/bin/activate
pip install -U pip
pip install torch-neuron==1.10.1.* neuron-cc[tensorflow] torchvision --extra-index-url=https://pip.repos.neuron.amazonaws.com
```

After installing the Inferentia neuron SDK, you will find `libtorchneuron.so` is installed in
`myenv/lib/python3.6/site-packages/torch_neuron/lib` folder.
You need configuration environment variable to enable Inferentia for DJL:

```
export PYTORCH_EXTRA_LIBRARY_PATH=$(python -m site | grep $VIRTUAL_ENV | awk -F"'" '{print $2}')/torch_neuron/lib/libtorchneuron.so
```

`libtorchneuron.so` depends on some shared library in its folder, you also need to specify LD_LIBRARY_PATH to make it work:

```
export LD_LIBRARY_PATH=$LD_LIBRARYPATH:$(python -m site | grep $VIRTUAL_ENV | awk -F"'" '{print $2}')/torch_neuron/lib/
```

## Compile your model into Neuron traced model

Use the following python script to trace a PyTorch resnet50 model. The script can also be found in the repo at [trace.py](https://github.com/deepjavalibrary/djl-demo/blob/master/aws/inferentia/trace.py).

```
import torch
import os
import torch_neuron
from torchvision import models
import logging

# Enable logging so we can see any important warnings
logger = logging.getLogger('Neuron')
logger.setLevel(logging.INFO)

# An example input you would normally provide to your model's forward() method.
image = torch.zeros([1, 3, 224, 224], dtype=torch.float32)

# Load a pretrained ResNet50 model
model = models.resnet50(pretrained=True)

# Tell the model we are using it for evaluation (not training)
model.eval()

# Use torch.jit.trace to generate a torch.jit.ScriptModule via tracing.
djl_traced_model = torch.jit.trace(model, image)

# Save the Regular TorchScript model for benchmarking
os.makedirs("models/djl/resnet50", exist_ok=True)
djl_traced_model.save("models/djl/resnet50/resnet50.pt")

# Analyze the model - this will show operator support and operator count
torch.neuron.analyze_model(model, example_inputs=[image])

# Now compile the model - with logging set to "info" we will see
# what compiles for Neuron, and if there are any fallbacks
model_neuron = torch.neuron.trace(model, example_inputs=[image])

# Export to saved model
os.makedirs("models/inferentia/resnet50", exist_ok=True)
model_neuron.save("models/inferentia/resnet50/resnet5.pt")
print("Compile success")
```

```
cd
git clone https://github.com/deepjavalibrary/djl-demo.git
cd djl-demo/aws/inferentia

python trace.py 
```

Execute above command, now you have a Neuron traced model ready for inference in 
`models/inferentia/resnet50` folder.

## Run example java program

```
cd djl-demo/aws/inferentia

./gradlew run

[INFO ] - Number of inter-op threads is 4
[INFO ] - Number of intra-op threads is 8
Running inference with PyTorch: 1.10.0
[
        class: "n02124075 Egyptian cat", probability: 0.41596
        class: "n02123159 tiger cat", probability: 0.26856
        class: "n02123045 tabby, tabby cat", probability: 0.23701
        class: "n02123394 Persian cat", probability: 0.04384
        class: "n02127052 lynx, catamount", probability: 0.00612
]
```

## Benchmark

You can use DJL benchmark tool to compare performance w/o Inferentia enabled:

### Run multi-threaded benchmark with Inferentia enabled:

```
./gradlew benchmark

[INFO ] - Running inference with PyTorch: 1.10.0
[INFO ] - Loading libneuron_op.so from: /home/ubuntu/myenv/lib/python3.6/site-packages/torch_neuron/lib/libtorchneuron.so
[INFO ] - Multithreaded inference with 8 threads.
[INFO ] - Throughput: 288.59, completed 8000 iteration in 27721 ms.
[INFO ] - Latency P50: 27.697 ms, P90: 27.915 ms, P99: 28.426 ms
```

### Run multi-thread benchmark with regular PyTorch model:

```
./gradlew benchmark --args="models/djl/resnet50"

[INFO ] - Running inference with PyTorch: 1.10.0
[INFO ] - Loading regular pytorch model ...
[INFO ] - Multithreaded inference with 8 threads.
[INFO ] - Throughput: 33.92, completed 8000 iteration in 235833 ms.
[INFO ] - Latency P50: 234.925 ms, P90: 257.252 ms, P99: 277.015 ms
```

### Run single-threaded benchmark with Inferentia enabled:

```
./gradlew benchmark --args="models/inferentia/resnet50 1"
```

### Run single-thread benchmark with regular PyTorch model:

```
./gradle benchmark --args="models/djl/resnet50 1"
```
