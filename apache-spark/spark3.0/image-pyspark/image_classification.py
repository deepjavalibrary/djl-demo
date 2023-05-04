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

    # Image classification
    classifier = ImageClassifier(input_cols=["origin", "height", "width", "nChannels", "mode", "data"],
                                 output_col="prediction",
                                 engine="PyTorch",
                                 model_url="djl://ai.djl.pytorch/resnet",
                                 top_k=2)
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
    #  |    |-- top_k: array (nullable = true)
    #  |    |    |-- element: string (containsNull = true)

    outputDf = outputDf.select("origin", "prediction.top_k")
    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").parquet(output_path)
    else:
        print("Printing results output stream")
        outputDf.show(truncate=False)
        # +-------------------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
        # |origin                                                       |top_k                                                                                                                                                                           |
        # +-------------------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
        # |s3://djl-ai/resources/demo/spark/image_classification/car.jpg|[class: "n03770679 minivan", probability: 0.93501, class: "n02814533 beach wagon, station wagon, wagon, estate car, beach waggon, station waggon, waggon", probability: 0.01280]|
        # |s3://djl-ai/resources/demo/spark/image_classification/dog.jpg|[class: "n02085936 Maltese dog, Maltese terrier, Maltese", probability: 0.99027, class: "n02098413 Lhasa, Lhasa apso", probability: 0.00534]                                    |
        # |s3://djl-ai/resources/demo/spark/image_classification/cat.jpg|[class: "n02123394 Persian cat", probability: 0.64565, class: "n02123045 tabby, tabby cat", probability: 0.18182]                                                               |
        # +-------------------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

    spark.stop()
