# Deep Java Library examples

[![DJL Demo Nightly test](https://github.com/deepjavalibrary/djl-demo/actions/workflows/nightly.yml/badge.svg)](https://github.com/deepjavalibrary/djl-demo/actions/workflows/nightly.yml)

The repository contains the source code of the examples for [Deep Java Library](http://djl.ai) (DJL) - an
framework-agnostic Java API for deep learning.

## Inference examples

### [Run python pre/post processing](development/python/README.md)

An example application show you how to run python code in DJL.

### [Malicious URL Detector](malicious-url-detector/README.md)

An example application detects malicious urls based on a trained Character Level CNN model.

### [Pneumonia Detection with TensorFlow engine](pneumonia-detection/README.md)

An example application detects Pneumonia based on X-ray images using a trained Keras model.

### [Live Object Detection](live-object-detection/README.md)

An example application detects live objects from web camera.

### [Online DoodleDraw game powered by DJL](web-demo/doodle_game)

A web based DoodleDraw game built with DJL.

### [Run deep learning in browser](web-demo/interactive-console/README.md)

A web application that runs DJL code in browser.

## Training examples

### [Train footwear classification with DJL](footwear_classification/README.md)

An example application trains footwear classification model using DJL.

### [Visualizing Training with DJL](visualization-vue/README.md)

An example application features a web UI to track and visualize metrics such as loss and accuracy.

## Android

### [Face detection made easy with DJL](android/pytorch_android/face_detection/README.md)

An example that shows how to build deep learning android app with ease.

### [Doodle draw app with PyTorch model](android/pytorch_android/quickdraw_recognition/README.md)

A Doodle draw android game that is built with PyTorch model.

### [Make your image look like a painting with Style Transfer](android/pytorch_android/style_transfer_cyclegan/README.md)

This app will take your image and convert it to the style of Van Gogh or Monet among others.

### [Identify objects with Semantic Segmentation](android/pytorch_android/semantic_segmentation/README.md)

An app that takes in an image and colors the objects in it.

### [Translate from French to English with Neural Machine Translation](android/pytorch_android/neural_machine_translation/README.md)

Write in the French text that you wanted translated and receive an English output.

### [Speech recognition](android/pytorch_android/speech_recognition/README.md)

An example that shows how to build speech recognition app.

### [Object detection with ONNX model](android/onnxruntime_android/object_detection/README.md)

An example that shows how to build object detection app with ONNX model.

### [Build Android app with MXNet engine](android/mxnet-android/README.md)

A template that can be used to build Android applications with MXNet engine.

## AWS services

### [AWS Kinesis Video Streams](aws/aws-kinesis-video-streams/README.md)

An example application that reads the output of a KVS Stream.

### [Serverless Model Serving with AWS Lambda](aws/lambda-model-serving/README.md)

An example application that serves deep learning model with AWS Lambda.

### [Model Serving on AWS Elastic Beanstalk](aws/beanstalk-model-serving/README.md)

Build a micro service to deploy on AWS Elastic Beanstalk.

### [Model Serving on AWS Elastic Beanstalk](aws/beanstalk-model-serving/README.md)

Build a micro service to deploy on AWS Elastic Beanstalk.

### [Low cost inference with AWS Inferentia](aws/inferentia/README.md)

An example application that runs low cost/high performance inference with AWS Inferentia.  

## Big data integration

### [Spark Image Classification](apache-spark/spark3.0/image/README.md)

Contains Spark image classification demos.

### [Apache Beam CTR Prediction](apache-beam/ctr-prediction/README.md)

An example application using [Apache Beam](https://beam.apache.org/) to predict the click-through rate for online advertisements.

### [Apache Flink Sentiment Analysis](apache-flink/sentiment-analysis/README.md)

An example using [Apache Flink](https://flink.apache.org/) to run sentiment analysis.

### [DJL Component in Apache Camel](camel-djl/README.md)

An example application that demonstrates simple HTTP-service to classify images using Zoo Model.

## Other demos

### [Use multiple engines in single JVM](development/multi-engine/README.md)

An example application that runs multiple deep learning frameworks in one Java Process.

### [Use graalvm to speed up your deep learning application](graalvm/README.md)

An example application that demonstrates compile DJL apps into native executables.

### [Deploy DJL models on Quarkus](quarkus/example/README.md)

An example application that serves deep learning models using Quarkus.

