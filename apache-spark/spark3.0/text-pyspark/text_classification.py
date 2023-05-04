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
from djl_spark.task.text import TextClassifier


if __name__ == "__main__":
    """
        Usage: text_classification.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("TextClassificationExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.createDataFrame(
        [
            (1, "Hello, y'all! How are you?"),
            (2, "Hello, my dog is cute")
        ],
        ["id", "text"]
    )

    # Text classification
    classifier = TextClassifier(input_col="text",
                                output_col="prediction",
                                engine="PyTorch",
                                model_url="djl://ai.djl.huggingface.pytorch/distilbert-base-uncased-finetuned-sst-2-english")
    outputDf = classifier.classify(df)
    outputDf.printSchema()
    # root
    #  |-- id: long (nullable = true)
    #  |-- text: string (nullable = true)
    #  |-- prediction: struct (nullable = true)
    #  |    |-- class_names: array (nullable = true)
    #  |    |    |-- element: string (containsNull = true)
    #  |    |-- probabilities: array (nullable = true)
    #  |    |    |-- element: double (containsNull = true)
    #  |    |-- top_k: array (nullable = true)
    #  |    |    |-- element: string (containsNull = true)

    outputDf = outputDf.select("text", "prediction.top_k")
    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").parquet(output_path)
    else:
        print("Printing results output stream")
        outputDf.show(truncate=False)
        # +--------------------------+----------------------------------------------------------------------------------------------+
        # |text                      |top_k                                                                                         |
        # +--------------------------+----------------------------------------------------------------------------------------------+
        # |Hello, y'all! How are you?|[{"class": "POSITIVE", "probability": 0.99722}, {"class": "NEGATIVE", "probability": 0.00277}]|
        # |Hello, my dog is cute     |[{"class": "POSITIVE", "probability": 0.99978}, {"class": "NEGATIVE", "probability": 0.00021}]|
        # +--------------------------+----------------------------------------------------------------------------------------------+

    spark.stop()
