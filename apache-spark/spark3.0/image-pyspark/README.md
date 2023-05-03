# DJL PySpark Image Example

## Introduction

This folder contains 3 demo applications built with Spark and DJL to run image related
tasks.

- image_classification: Ready to run for image classification
- object_detection: Ready to run for object detection
- semantic_segmentation: Ready to run for semantic segmentation

## Run the example

Use `spark-submit` to run the examples. For example, to run the image classification example, you can run:

```
spark-submit \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.executor.memory=2G \
    --conf spark.executor.cores=2 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    image_classification.py
```

Refer [here](https://github.com/deepjavalibrary/djl-demo/blob/master/aws/sagemaker/processing/djl_sagemaker_spark_processing_image_classification/djl_sagemaker_spark_processing_image_classification.ipynb)
on how to run this example on Amazon SageMaker Processing.
