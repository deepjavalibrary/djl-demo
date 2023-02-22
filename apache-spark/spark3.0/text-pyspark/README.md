# DJL Spark Text Example

## Introduction
This folder contains demo applications built with Spark 3.0 and DJL to run example text tasks.

- text_embedding: Example to run text embedding on Spark.
- text_encoding: Example to run text encoding / decoding on Spark.
- text_tokenization: Example to run text tokenization on Spark.

## Run the example

Use `spark-submit` to run the examples. For example, to run the text embedding example, you can run:

```
spark-submit \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.executor.memory=2G \
    --conf spark.executor.cores=2 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    text_embedding.py
```

Refer to the [Set up EMR on EKS](../image-classification-pyspark/README.md) if you want to run this example
on EMR on EKS.
