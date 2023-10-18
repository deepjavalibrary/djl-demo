# Low cost inference with AWS Inferentia

[AWS Inferentia](https://aws.amazon.com/machine-learning/inferentia/) is a high performance machine
learning inference chip, custom designed by AWS. Amazon EC2 Inf1 instances are powered by AWS
Inferentia chips, which provides you with the lowest cost per inference in the cloud and lower
the barriers for everyday developers to use machine learning (ML) at scale. Customers using models
such as YOLO v3 and YOLO v4 can get up to 1.85 times higher throughput and up to 40% lower cost per
inference compared to the EC2 G4 GPU-based instances.

In the demo, you will learn how to run PyTorch model with DJL on Amazon EC2 Inf1 instances.

## Setup environment

### Launch Inf1 EC2 instance

Please launch Inf1 instance by following the [Install Neuron Instructions](https://awsdocs-neuron.readthedocs-hosted.com/en/latest/frameworks/torch/torch-neuron/setup/pytorch-install.html)

This demo tested on Neuron SDK 2.5.0 and PyTorch 1.12.1 on Ubuntu DLAMI.
Please make sure you have Neuron Runtime 2.x installed:

```
# Configure Linux for Neuron repository updates
. /etc/os-release

sudo tee /etc/apt/sources.list.d/neuron.list > /dev/null <<EOF
deb https://apt.repos.neuron.amazonaws.com ${VERSION_CODENAME} main
EOF
curl -L https://apt.repos.neuron.amazonaws.com/GPG-PUB-KEY-AMAZON-AWS-NEURON.PUB | sudo apt-key add -

# Update OS packages
sudo apt-get update -y

sudo apt-get install aws-neuronx-dkms -y
sudo apt-get install aws-neuronx-tools -y
```

### Install AWS Inferentia neuron SDK

The Inferentia neuron SDK is required for converting PyTorch pre-trained model into neuron traced model.

```
python3 -m venv myenv

source myenv/bin/activate
pip install -U pip

pip install torch-neuron==1.12.1.* neuron-cc[tensorflow] torchvision --extra-index-url=https://pip.repos.neuron.amazonaws.com
```

After installing the Inferentia neuron SDK, you will find `libtorchneuron.so` is installed in
`myenv/lib/python3.8/site-packages/torch_neuron/lib` folder.
You need configuration environment variable to enable Inferentia for DJL:

```
export PYTORCH_EXTRA_LIBRARY_PATH=$(python -m site | grep $VIRTUAL_ENV | awk -F"'" 'END{print $2}')/torch_neuron/lib/libtorchneuron.so
```

## Compile your model into Neuron traced model

Use the following python script to trace a PyTorch resnet50 model. The script can also be found in the repo at [trace_inf1.py](https://github.com/deepjavalibrary/djl-demo/blob/master/aws/inferentia/trace_inf1.py).

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
model = models.resnet50(weights='ResNet50_Weights.DEFAULT')

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
git clone https://github.com/deepjavalibrary/djl-demo.git
cd djl-demo/aws/inferentia

python trace_inf1.py
```

Execute above command, now you have a Neuron traced model ready for inference in
`models/inferentia/resnet50` folder.

## Run example java program

*Notes:* in torch-neuron 1.11.x and 1.12.x, the stack size must be set to 2M and more. The default
JVM stack size is 1M, you have to explicitly set the JVM arguments: `-Xss2m` 

```
cd djl-demo/aws/inferentia

# inf1 only support PyTorch 1.12.1
export PYTORCH_VERSION=1.12.1

./gradlew run

[INFO ] - Number of inter-op threads is 4
[INFO ] - Number of intra-op threads is 8
Running inference with PyTorch: 1.12.1
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

### Run multithreading benchmark with Inferentia enabled:

```
# inf1 only support PyTorch 1.12.1
export PYTORCH_VERSION=1.12.1

./gradlew benchmark

[INFO ] - Running inference with PyTorch: 1.12.1
[INFO ] - Loading libneuron_op.so from: /home/ubuntu/myenv/lib/python3.8/site-packages/torch_neuron/lib/libtorchneuron.so
[INFO ] - Multithreaded inference with 8 threads.
[INFO ] - Throughput: 288.59, completed 8000 iteration in 27721 ms.
[INFO ] - Latency P50: 27.697 ms, P90: 27.915 ms, P99: 28.426 ms
```

### Run multi-thread benchmark with regular PyTorch model:

```
# inf1 only support PyTorch 1.12.1
export PYTORCH_VERSION=1.12.1

./gradlew benchmark --args="models/djl/resnet50 8"

[INFO ] - Running inference with PyTorch: 1.12.1
[INFO ] - Loading regular pytorch model ...
[INFO ] - Multithreaded inference with 8 threads.
[INFO ] - Throughput: 33.92, completed 8000 iteration in 235833 ms.
[INFO ] - Latency P50: 234.925 ms, P90: 257.252 ms, P99: 277.015 ms
```

### Run single-threaded benchmark with Inferentia enabled:

```
# inf1 only support PyTorch 1.12.1
export PYTORCH_VERSION=1.12.1

./gradlew benchmark --args="models/inferentia/resnet50 1"
```

### Run single-thread benchmark with regular PyTorch model:

```
./gradle benchmark --args="models/djl/resnet50 1"
```
