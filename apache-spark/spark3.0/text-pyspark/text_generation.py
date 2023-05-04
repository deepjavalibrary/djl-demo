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
from djl_spark.task.text import TextGenerator


if __name__ == "__main__":
    """
        Usage: text_generation.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("TextGenerationExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    # Input
    df = spark.createDataFrame(
        [
            (1, "My name is Julien and I like to"),
            (2, "My name is Thomas and my main"),
            (3, "My name is Mariama, my favorite")
        ],
        ["id", "text"]
    )
    df.show(truncate=False)
    # +---+-------------------------------+
    # |id |text                           |
    # +---+-------------------------------+
    # |1  |My name is Julien and I like to|
    # |2  |My name is Thomas and my main  |
    # |3  |My name is Mariama, my favorite|
    # +---+-------------------------------+

    # Text generation
    generator = TextGenerator(input_col="text",
                              output_col="prediction",
                              hf_model_id="facebook/opt-125m")
    outputDf = generator.generate(df, do_sample=True, max_length=30)

    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").parquet(output_path)
    else:
        print("Printing results output stream")
        outputDf.printSchema()
        # root
        #  |-- id: long (nullable = true)
        #  |-- text: string (nullable = true)
        #  |-- prediction: string (nullable = true)

        outputDf.show(truncate=False)
        # +---+-------------------------------+---------------------------------------------------------------------------------------------------------------------------------+
        # |id |text                           |prediction                                                                                                                       |
        # +---+-------------------------------+---------------------------------------------------------------------------------------------------------------------------------+
        # |1  |My name is Julien and I like to|My name is Julien and I like to travel to some weird place in the world where there is nowhere to go or where I can't have fun   |
        # |2  |My name is Thomas and my main  |My name is Thomas and my main purpose is to be a good friend to you.\nIt is my intention to try to make you understand the things|
        # |3  |My name is Mariama, my favorite|My name is Mariama, my favorite anime is YuGiOh! and I enjoy writing, I used to be a manga fan, I want to                        |
        # +---+-------------------------------+---------------------------------------------------------------------------------------------------------------------------------+

    spark.stop()
