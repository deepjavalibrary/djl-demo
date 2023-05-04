# DJL PySpark Text Example

## Introduction

This folder contains demo applications built with Spark and DJL to run example text related tasks.

- question_answering: Example to run question answering on Spark.
- text2text_generation_alpaca: Example to run text2text generation with Flan-Alpaca model on Spark.
- text2text_generation_t5: Example to run text2text generation with Flan-T5 model on Spark.
- text_classification: Example to run text classification on Spark.
- text_embedding: Example to run text embedding on Spark.
- text_encoding: Example to run text encoding / decoding on Spark.
- text_generation: Example to run text generation on Spark.
- text_tokenization: Example to run text tokenization on Spark.

## Run the example

Use `spark-submit` to run the examples. For example, to run the text classification example, you can run:

```
spark-submit \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.executor.memory=2G \
    --conf spark.executor.cores=2 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    text_classification.py
```

Refer [here](https://github.com/deepjavalibrary/djl-demo/blob/master/aws/sagemaker/processing/djl_sagemaker_spark_processing_text_classification/djl_sagemaker_spark_processing_text_classification.ipynb)
on how to run this example on Amazon SageMaker Processing.
