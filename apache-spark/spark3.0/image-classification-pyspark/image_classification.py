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
from pyspark.sql.session import SparkSession
from djl_spark.task.vision import ImageClassifier


if __name__ == "__main__":
    """
        Usage: image_classification.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("ImageClassificationExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.read.format("image").option("dropInvalid", True).load("s3://djl-ai/resources/demo/spark/image_classification/")
    df.printSchema()
    # root
    # |-- image: struct (nullable = true)
    # |    |-- origin: string (nullable = true)
    # |    |-- height: integer (nullable = true)
    # |    |-- width: integer (nullable = true)
    # |    |-- nChannels: integer (nullable = true)
    # |    |-- mode: integer (nullable = true)
    # |    |-- data: binary (nullable = true)

    df = df.select("image.*").filter("nChannels=3") # The model expects RGB images

    classifier = ImageClassifier(input_cols=["origin", "height", "width", "nChannels", "mode", "data"],
                                 output_col="prediction",
                                 engine="PyTorch",
                                 model_url="djl://ai.djl.pytorch/resnet",
                                 topK=2)
    outputDf = classifier.classify(df)
    outputDf.printSchema()
    # root
    #  |-- origin: string (nullable = true)
    #  |-- height: integer (nullable = true)
    #  |-- width: integer (nullable = true)
    #  |-- nChannels: integer (nullable = true)
    #  |-- mode: integer (nullable = true)
    #  |-- data: binary (nullable = true)
    #  |-- prediction: struct (nullable = true)
    #  |    |-- class_names: array (nullable = true)
    #  |    |    |-- element: string (containsNull = true)
    #  |    |-- probabilities: array (nullable = true)
    #  |    |    |-- element: double (containsNull = true)
    #  |    |-- topK: map (nullable = true)
    #  |    |    |-- key: string
    #  |    |    |-- value: double (valueContainsNull = true)

    outputDf = outputDf.select("origin", "prediction.topK")
    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").orc(output_path)
    else:
        print("Printing results output stream")
        outputDf.show(truncate=False)
        # +-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
        # |origin                                                       |topK                                                                                                                                                           |
        # +-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
        # |s3://djl-ai/resources/demo/spark/image_classification/car.jpg|{n03770679 minivan -> 0.8499245047569275, n02814533 beach wagon, station wagon, wagon, estate car, beach waggon, station waggon, waggon -> 0.04071871191263199}|
        # |s3://djl-ai/resources/demo/spark/image_classification/dog.jpg|{n02085936 Maltese dog, Maltese terrier, Maltese -> 0.7925896644592285, n02113624 toy poodle -> 0.11378670483827591}                                           |
        # |s3://djl-ai/resources/demo/spark/image_classification/cat.jpg|{n02123394 Persian cat -> 0.9232246279716492, n02127052 lynx, catamount -> 0.05277140066027641}                                                                |
        # +-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+

    spark.stop()
