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
from djl_spark.task.text import TextEmbedder


if __name__ == "__main__":
    """
        Usage: text_embedding.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("TextEmbeddingExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.createDataFrame(
        [
            ("1", "Hello, y'all! How are you?"),
            ("2", "Hello to you too!"),
            ("3", "I'm fine, thank you!")
        ],
        ["id", "text"]
    )
    df.show(truncate=False)
    # +---+--------------------------+
    # |id |text                      |
    # +---+--------------------------+
    # |1  |Hello, y'all! How are you?|
    # |2  |Hello to you too!         |
    # |3  |I'm fine, thank you!      |
    # +---+--------------------------+

    # Embedding
    embedder = TextEmbedder(input_col="text",
                            output_col="prediction",
                            engine="TensorFlow",
                            model_url="https://storage.googleapis.com/tfhub-modules/google/universal-sentence-encoder/4.tar.gz")
    outputDf = embedder.embed(df)

    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").orc(output_path)
    else:
        print("Printing results output stream")
        outputDf.printSchema()
        # root
        #  |-- id: string (nullable = true)
        #  |-- text: string (nullable = true)
        #  |-- embedding: array (nullable = true)
        #  |    |-- element: float (containsNull = true)

        outputDf.show(truncate=False)
        # +---+--------------------+--------------------+
        # | id|                text|           embedding|
        # +---+--------------------+--------------------+
        # |  1|Hello, y'all! How...|[-0.05769434, -0....|
        # |  2|   Hello to you too!|[0.012077843, -0....|
        # |  3|I'm fine, thank you!|[-0.040518265, -0...|
        # +---+--------------------+--------------------+

    spark.stop()
