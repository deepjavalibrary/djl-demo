#!/usr/bin/env python
#
# Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
# except in compliance with the License. A copy of the License is located at
#
# http://aws.amazon.com/apache2.0/
#
# or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for
# the specific language governing permissions and limitations under the License.

import sys
import pandas as pd
import numpy as np
from PIL import Image

from typing import Iterator

from pyspark.sql.session import SparkSession
from pyspark.sql.functions import udf
from pyspark.sql.types import StructType, StructField, BinaryType, FloatType, ArrayType

from djl_spark.task.binary import BinaryPredictor
from djl_spark.util import np_util


def transform(img_data):
    img = Image.frombytes(mode='RGB', data=img_data.data, size=[img_data.width, img_data.height])

    # Resize
    img = img.resize([224, 224], resample=Image.Resampling.BICUBIC)

    # BGR to RGB
    arr = np.flip(np.asarray(img), axis=2)

    # ToTensor operation
    arr = np.expand_dims(arr, axis=0)
    arr = np.divide(arr, 255.0)
    arr = arr.transpose((0, 3, 1, 2))
    arr = np.squeeze(arr, axis=0)
    arr = np.float32(arr)
    return np_util.to_npz([arr])


def transform_udf(iterator: Iterator[pd.DataFrame]) -> Iterator[pd.DataFrame]:
    for batch in iterator:
        batch["transformed_image"] = batch.apply(transform, axis=1)
        yield batch


def softmax(data):
    arr = np_util.from_npz(data)[0]
    prob = np.exp(arr) / np.sum(np.exp(arr), axis=0)
    return prob.tolist()


if __name__ == "__main__":
    """
        Usage: image_classification_np.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.read.format("image").option("dropInvalid", True).load("s3://djl-ai/resources/demo/spark/image_classification")
    df.printSchema()
    # root
    # |-- image: struct (nullable = true)
    # |    |-- origin: string (nullable = true)
    # |    |-- height: integer (nullable = true)
    # |    |-- width: integer (nullable = true)
    # |    |-- nChannels: integer (nullable = true)
    # |    |-- mode: integer (nullable = true)
    # |    |-- data: binary (nullable = true)

    df = df.select("image.*").filter("nChannels=3")  # The model expects RGB images

    # Pre-processing
    schema = StructType(df.select("*").schema.fields + [
        StructField("transformed_image", BinaryType(), True)
    ])
    transformed_df = df.mapInPandas(transform_udf, schema)

    # Inference
    predictor = BinaryPredictor(input_col="transformed_image",
                                output_col="prediction",
                                engine="PyTorch",
                                model_url="djl://ai.djl.pytorch/resnet",
                                batchifier="stack")
    outputDf = predictor.predict(transformed_df)

    # Post-processing
    softmax_udf = udf(softmax, ArrayType(FloatType()))
    outputDf = outputDf.withColumn("probabilities", softmax_udf(outputDf['prediction']))
    outputDf.printSchema()
    # root
    #  |-- origin: string (nullable = true)
    #  |-- height: integer (nullable = true)
    #  |-- width: integer (nullable = true)
    #  |-- nChannels: integer (nullable = true)
    #  |-- mode: integer (nullable = true)
    #  |-- data: binary (nullable = true)
    #  |-- transformed_image: binary (nullable = true)
    #  |-- prediction: binary (nullable = true)
    #  |-- probabilities: array (nullable = true)
    #  |    |-- element: float (containsNull = true)

    outputDf = outputDf.select("origin", "probabilities")
    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").parquet(output_path)
    else:
        print("Printing results output stream")
        outputDf.show()
        # +--------------------+--------------------+
        # |              origin|       probabilities|
        # +--------------------+--------------------+
        # |s3://djl-ai/resou...|[1.9199406E-5, 4....|
        # |s3://djl-ai/resou...|[8.3182664E-7, 2....|
        # |s3://djl-ai/resou...|[3.0311656E-10, 3...|
        # +--------------------+--------------------+
