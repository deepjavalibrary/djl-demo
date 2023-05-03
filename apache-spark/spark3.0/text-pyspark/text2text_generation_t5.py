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
from djl_spark.task.text import Text2TextGenerator


if __name__ == "__main__":
    """
        Usage: text2text_generation_t5.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("Text2TextGenerationT5Example") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.createDataFrame(
        [
            (1, "Give three tips for staying healthy."),
            (2, "What are the three primary colors?"),
            (3, "Describe the structure of an atom."),
            (4, "How can we reduce air pollution?"),
            (5, "Describe a time when you had to make a difficult decision.")
        ],
        ["id", "text"]
    )
    df.show(truncate=False)
    # +---+----------------------------------------------------------+
    # |id |text                                                      |
    # +---+----------------------------------------------------------+
    # |1  |Give three tips for staying healthy.                      |
    # |2  |What are the three primary colors?                        |
    # |3  |Describe the structure of an atom.                        |
    # |4  |How can we reduce air pollution?                          |
    # |5  |Describe a time when you had to make a difficult decision.|
    # +---+----------------------------------------------------------+

    # Text2Text generation using Flan-T5 model
    generator = Text2TextGenerator(input_col="text",
                                   output_col="prediction",
                                   hf_model_id="google/flan-t5-small")
    outputDf = generator.generate(df, do_sample=True, max_length=128)

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
        # +---+----------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------+
        # |id |text                                                      |prediction                                                                                                            |
        # +---+----------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------+
        # |1  |Give three tips for staying healthy.                      |Keep on eating to try something new and more nutritious.                                                              |
        # |2  |What are the three primary colors?                        |white                                                                                                                 |
        # |3  |Describe the structure of an atom.                        |An atom (or "i") is a molecule of matter.                                                                             |
        # |4  |How can we reduce air pollution?                          |Use utensils and other products that cover the roof and the area and the air.                                         |
        # |5  |Describe a time when you had to make a difficult decision.|In this article you would say, the time your decisions were made was never too difficult. This is not all of a sudden.|
        # +---+----------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------+

    spark.stop()
