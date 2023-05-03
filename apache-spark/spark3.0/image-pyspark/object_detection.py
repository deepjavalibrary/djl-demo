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
from djl_spark.task.vision import ObjectDetector


if __name__ == "__main__":
    """
        Usage: object_detection.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("ObjectDetectionExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.read.format("image").option("dropInvalid", True).load("s3://djl-ai/resources/images/dog_bike_car.jpg")
    df.printSchema()
    # root
    # |-- image: struct (nullable = true)
    # |    |-- origin: string (nullable = true)
    # |    |-- height: integer (nullable = true)
    # |    |-- width: integer (nullable = true)
    # |    |-- nChannels: integer (nullable = true)
    # |    |-- mode: integer (nullable = true)
    # |    |-- data: binary (nullable = true)

    # Object detection
    detector = ObjectDetector(input_cols=["origin", "height", "width", "nChannels", "mode", "data"],
                              output_col="prediction",
                              engine="PyTorch",
                              model_url="djl://ai.djl.pytorch/yolov5s")
    outputDf = detector.detect(df)
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
    #  |    |-- bounding_boxes: array (nullable = true)
    #  |    |    |-- element: string (containsNull = true)

    outputDf = outputDf.select("origin", "prediction.bounding_boxes")
    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").parquet(output_path)
    else:
        print("Printing results output stream")
        outputDf.show(truncate=False)
        # +---------------------------------------------+--------------------------------------------------------------------------------------------------------------+
        # |origin                                       |bounding_boxes                                                                                                |
        # +---------------------------------------------+--------------------------------------------------------------------------------------------------------------+
        # |s3://djl-ai/resources/images/dog_bike_car.jpg|[{"x"=0.613, "y"=0.133, "width"=0.283, "height"=0.165}, {"x"=0.170, "y"=0.375, "width"=0.235, "height"=0.579}]|
        # +---------------------------------------------+--------------------------------------------------------------------------------------------------------------+

    spark.stop()
