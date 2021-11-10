## Serving Huggingface model with AWS Inferentia

[AWS Inferentia](https://aws.amazon.com/machine-learning/inferentia/) is a high performance machine
learning inference chip, custom designed by AWS. Amazon EC2 Inf1 instances are powered by AWS
Inferentia chips, which provides you with the lowest cost per inference in the cloud and lower
the barriers for everyday developers to use machine learning (ML) at scale.

In the demo, you will learn how to deploy PyTorch model with **djl-serving** on Amazon EC2 Inf1 instances.
You can follow [this instruction](deploy_on_sagemaker.md) if you want to deploy your model on AWS SageMaker.

## Setup environment

### Launch Inf1 EC2 instance

Please launch Inf1 instance by following the [Install Neuron Instructions](https://awsdocs-neuron.readthedocs-hosted.com/en/latest/neuron-intro/pytorch-setup/pytorch-install.html#install-neuron-pytorch)

This demo tested on Neuron SDK 1.16.0 and PyTorch 1.9.1 on Ubuntu DLAMI.
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
pip install torchvision torch-neuron==1.9.1.2.0.392.0 'neuron-cc[tensorflow]==1.7.3.0' --extra-index-url=https://pip.repos.neuron.amazonaws.com
```

## Compile your model into Neuron traced model

Use the following command to trace a Huggingface questing answering model. The script can be found in the repo at [trace.py](trace.py).
You can also download a traced model from: https://resources.djl.ai/test-models/pytorch/bert_qa_inf1.tar.gz

You can find more details on [Neuron tutorial](https://awsdocs-neuron.readthedocs-hosted.com/en/latest/neuron-guide/neuron-frameworks/pytorch-neuron/tutorials/index.html). 

```
cd huggingface/inferentia
python trace.py 
```

Execute above command, now you have a Neuron traced model `model.pt` ready for inference in `questions_answering` folder.

## Install djl-serving

This demo requires **djl-serving** 0.14.0 (not release yet). You need to run **djl-serving** from source.

```
git clone https://github.com/deepjavalibrary/djl-serving.git
```

## Deploy question answering model with DJL

**djl-serving** allows you run Huggingface model in both Java and Python engine.

### Run java engine

After installing the Inferentia neuron SDK, you will find `libtorchneuron.so` is installed in
`myenv/lib/python3.6/site-packages/torch_neuron/lib` folder.
You need configuration environment variable to enable Inferentia for DJL:

```
export PYTORCH_EXTRA_LIBRARY_PATH=$(python -m site | grep $VIRTUAL_ENV | awk -F"'" '{print $2}')/torch_neuron/lib/libtorchneuron.so
```

`libtorchneuron.so` depends on some shared library in its folder, you also need to specify `LD_LIBRARY_PATH` to make it work:

```
export LD_LIBRARY_PATH=$LD_LIBRARYPATH:$(python -m site | grep $VIRTUAL_ENV | awk -F"'" '{print $2}')/torch_neuron/lib/
```

Neuron SDK requires precxx11 version of PyTorch native library, you need set the
following environment variable to instruct DJL load precxx11 PyTorch native library:

```
export PYTORCH_PRECXX11=true
```

You can use **djl-serving** to deploy your question answering model out of the box. It will serve
your model in a tensor in tensor out fashion. You can leverage DJL built-in pre-processing and
post-processing to performance BERT Tokenize for you. You can simply provide a `serving.properties`
in the model directory:

```
translatorFactory=ai.djl.pytorch.zoo.nlp.qa.PtBertQATranslatorFactory
tokenizer=distilbert
padding=true
max_length=128
```

**Note:** `padding=true` is required for using neuron sdk, since neuron traced model using a fixed input shape.

```
cd djl-serving/serving

./gradlew :serving:run --args="-m bert_qa::PyTorch:*=file:$HOME/source/djl-demo/huggingface/inferentia/question_answering"
```

### Run Python engine

**djl-serving**'s Python engine is compatible with [TorchServe](https://github.com/pytorch/serve) `.mar` file.
You can deploy TorchServe `.mar` directly in **djl-serving**.

**djl-serving** Python model's script format is similar to TorchServe, but simpler.
See [DJL Python engine](https://github.com/deepjavalibrary/djl-serving/tree/master/engines/python) for how to
write DJL Python model.

```
cd djl-serving/serving

./gradlew :serving:run --args="-m bert_qa::Python:*=file:$HOME/source/djl-demo/huggingface/inferentia/question_answering"
```

## Run inference

**djl-serving** provides a [REST API](https://github.com/deepjavalibrary/djl-serving/blob/master/serving/docs/inference_api.md) allows user to run inference.
The API is compatible with [TorchServe](https://github.com/pytorch/serve) and [MMS](https://github.com/awslabs/multi-model-server). 

```
curl -X POST http://127.0.0.1:8080/predictions/bert_qa \
    -H "Content-Type: application/json" \
    -d '{"question": "How is the weather", "paragraph": "The weather is nice, it is beautiful day"}'    
```

## Benchmark

We use apache bench to run benchmark testing:

```
sudo apt-get install -y apache2-utils

echo '{"question": "How is the weather", "paragraph": "The weather is nice, it is beautiful day"}' > qa_payload.json
ab -c 8 -n 8000 -k -p qa_payload.json \
    -T "application/json" \
    "http://127.0.0.1:8080/predictions/bert_qa"
```

## Performance

By default, **djl-serving** will assume the model compiled for one NeuronCore. When you specify "*" in `-m`
command line parameter, DJL will automatically detect the number of NeuronCores available in the system
and load the model on each NeuronCore.

You can control number of NeuronCores to use for the model, use the following command to start **djl-serving**:

```
# java engine
./gradlew :serving:run --args="-m bert_qa::PyTorch:nc0;nc1=file:$HOME/source/djl-demo/huggingface/inferentia/question_answering"

# python engine
./gradlew :serving:run --args="-m bert_qa::Python:n2,nc3=file:$HOME/source/djl-demo/huggingface/inferentia/question_answering"
```

If your model is traced with 2 NeuronCores, you can use the following command:

```
# java engine
./gradlew :serving:run --args="-m bert_qa::PyTorch:nc0-1;nc2-3=file:$HOME/source/djl-demo/huggingface/inferentia/question_answering"

# python engine
./gradlew :serving:run --args="-m bert_qa::Python:nc0-1;nc2-3=file:$HOME/source/djl-demo/huggingface/inferentia/question_answering"
```


