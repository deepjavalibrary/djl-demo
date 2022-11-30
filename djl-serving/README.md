# DJL Serving demo

In this folder, we will show case the four popular operation modes in DJL. There are four operating modes supported by DJL:

- [Python mode](https://github.com/deepjavalibrary/djl-demo/tree/master/djl-serving/python-mode/README.md): User just need to prepare a python script file to execute.
- Python Hybrid mode (under development): User can offer a python script to do pre/post processing with a model file.
- [Java mode](https://github.com/deepjavalibrary/djl-demo/tree/master/djl-serving/java-mode/README.md): User need to prepare a Java pre/post processing script and a model file.
- [Binary mode](https://github.com/deepjavalibrary/djl-demo/tree/master/djl-serving/binary-mode/README.md): User just need to prepare a model file, we will just run tensor-in, tensor out operation.

## Installation

### macOS

```
brew install djl-serving
# run serving
djl-serving
```

### Linux

```
curl -O https://publish.djl.ai/djl-serving/djl-serving_0.20.0-1_all.deb
sudo dpkg -i djl-serving_0.20.0-1_all.deb
# run serving
djl-serving
```

### Windows

```
curl -O https://publish.djl.ai/djl-serving/serving-0.20.0.zip
unzip serving-0.20.0.zip
# start djl-serving
serving-0.20.0\bin\serving.bat
```

## DJL Serving WorkLoadManager

There are also some demos of the [DJL Serving WorkLoadManager](https://github.com/deepjavalibrary/djl-serving/tree/master/wlm)

- [Sentiment Analysis with Flink](wlm/flink-sentiment-analysis/README.md)
