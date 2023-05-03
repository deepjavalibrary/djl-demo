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
from djl_spark.task.vision import SemanticSegmenter


if __name__ == "__main__":
    """
        Usage: semantic_segmentation.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("SemanticSegmentationExample") \
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

    segmenter = SemanticSegmenter(input_cols=["origin", "height", "width", "nChannels", "mode", "data"],
                                  output_col="prediction",
                                  engine="PyTorch",
                                  model_url="djl://ai.djl.pytorch/deeplabv3")
    outputDf = segmenter.segment(df)
    outputDf.printSchema()
    # root
    #  |-- origin: string (nullable = true)
    #  |-- height: integer (nullable = true)
    #  |-- width: integer (nullable = true)
    #  |-- nChannels: integer (nullable = true)
    #  |-- mode: integer (nullable = true)
    #  |-- data: binary (nullable = true)
    #  |-- prediction: struct (nullable = true)
    #  |    |-- classes: array (nullable = true)
    #  |    |    |-- element: string (containsNull = true)
    #  |    |-- mask: array (nullable = true)
    #  |    |    |-- element: array (containsNull = true)
    #  |    |    |    |-- element: integer (containsNull = true)

    outputDf = outputDf.select("origin", "prediction.*")
    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").parquet(output_path)
    else:
        print("Printing results output stream")
        outputDf.show()
        # +--------------------+--------------------+--------------------+
        # |              origin|             classes|                mask|
        # +--------------------+--------------------+--------------------+
        # |s3://djl-ai/resou...|[__background__, ...|[[0, 0, 0, 0, 0, ...|
        # +--------------------+--------------------+--------------------+

    spark.stop()
