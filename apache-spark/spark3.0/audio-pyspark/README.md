# DJL PySpark Audio Example

## Introduction

This folder contains 2 demo applications built with Spark and DJL to run audio related
tasks.

- speech_recognition: Ready to run for speech recognition using Wav2Vec2 model
- whisper_speech_recognition: Ready to run for speech recognition using Whisper model

## Run the example

Use `spark-submit` to run the examples. For example, to run the whisper speech recognition example, you can run:

```
spark-submit \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.executor.memory=2G \
    --conf spark.executor.cores=2 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    whisper_speech_recognition.py
```

Refer [here](https://github.com/deepjavalibrary/djl-demo/blob/master/aws/sagemaker/processing/djl_sagemaker_spark_processing_whisper_speech_recognition/djl_sagemaker_spark_processing_whisper_speech_recognition.ipynb)
on how to run this example on Amazon SageMaker Processing.
