# DJL Serving demo

In this folder, we will showcase the four popular operation modes in DJL Serving. There are four operating modes supported:

- [Python mode](python-mode/README.md): User just need to prepare a python script file to execute.
- Python Hybrid mode (under development): User can offer a python script to do pre/post processing with a model file.
- [Java mode](java-mode/README.md): User need to prepare a Java pre/post processing script and a model file.
- [Binary mode](binary-mode/README.md): User just need to prepare a model file, we will just run tensor-in, tensor out operation.

## Installation

### macOS

```
brew install djl-serving
# run serving
djl-serving
```

### Linux

```
curl -O https://publish.djl.ai/djl-serving/djl-serving_0.21.0-1_all.deb
sudo dpkg -i djl-serving_0.21.0-1_all.deb
# run serving
djl-serving
```

### Windows

```
curl -O https://publish.djl.ai/djl-serving/serving-0.21.0.zip
unzip serving-0.21.0.zip
# start djl-serving
serving-0.21.0\bin\serving.bat
```

## DJL Serving Client

We also demo DJL Serving Client applications that run inference in various formats:

- [Python client](python-client/README.md)
- [Java client](java-client/README.md)
- [Postman client](postman-client/README.md)

## DJL Serving WorkLoadManager

There are also some demos of the [DJL Serving WorkLoadManager](https://github.com/deepjavalibrary/djl-serving/tree/master/wlm)

- [Sentiment Analysis with Flink](wlm/flink-sentiment-analysis/README.md)

## DJL Serving Workflow

There are also some demos of the DJLServing Workflows.

- [Python Workflow](workflows/python/README.md): User just need to prepare a python model to run in a workflow.
- [Python Pre/Post Processing Workflow](workflows/python-pre-post-processing/README.md): User can offer a python script to do pre/post processing with a model file to run in a workflow.
- [Java Pre/Post Processing Workflow](workflows/java-pre-post-processing/README.md): User need to prepare a Java pre/post processing code and a model file to run in a workflow.
- [Multi-model Workflow](workflows/multi-model/README.md): Multiple models can be loaded and run in a workflow.
