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
from djl_spark.task.text import QuestionAnswerer


if __name__ == "__main__":
    """
        Usage: question_answering.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("QuestionAnsweringExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.createDataFrame(
        [
            (1, "Who was Jim Henson?", "Jim Henson was a puppeteer"),
            (2, "Where do I live?", "My name is Wolfgang and I live in Berlin")
        ],
        ["id", "question", "context"]
    )

    # Question answering
    answerer = QuestionAnswerer(input_cols=["question", "context"],
                                output_col="answer",
                                engine="PyTorch",
                                model_url="djl://ai.djl.huggingface.pytorch/deepset/bert-base-cased-squad2")
    outputDf = answerer.answer(df)

    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").csv(output_path)
    else:
        print("Printing results output stream")
        outputDf.printSchema()
        # root
        #  |-- id: long (nullable = true)
        #  |-- question: string (nullable = true)
        #  |-- context: string (nullable = true)
        #  |-- answer: string (nullable = true)

        outputDf.show(truncate=False)
        # +---+-------------------+----------------------------------------+-----------+
        # |id |question           |paragraph                               |answer     |
        # +---+-------------------+----------------------------------------+-----------+
        # |1  |Who was Jim Henson?|Jim Henson was a puppeteer              |a puppeteer|
        # |2  |Where do I live?   |My name is Wolfgang and I live in Berlin|Berlin     |
        # +---+-------------------+----------------------------------------+-----------+

    spark.stop()
