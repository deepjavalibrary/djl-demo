# DJL Serving demo

In this folder, we will show case the four popular operation modes in DJL. There are four operating modes supported by DJL:

- Python mode: User just need to prepare a python script file to execute.
- Python Hybrid mode (under development): User can offer a python script to do pre/post processing with a model file.
- Java mode: User need to prepare a Java pre/post processing script and a model file.
- Binary mode: User just need to prepare a model file, we will just run tensor-in, tensor out operation.

## Installation

### macOS

```
brew install djl-serving
# run serving
djl-serving
```

### Linux

```
curl -O https://publish.djl.ai/djl-serving/djl-serving_0.16.0-1_all.deb
sudo dpkg -i djl-serving_0.16.0-1_all.deb
# run serving
djl-serving
```

### Windows

```
curl -O https://publish.djl.ai/djl-serving/serving-0.16.0.zip
unzip serving-0.16.0.zip
# start djl-serving
serving-0.16.0\bin\serving.bat
```
