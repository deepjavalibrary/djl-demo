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
from djl_spark.task.text import HuggingFaceTextTokenizer


if __name__ == "__main__":
    """
        Usage: text_tokenization.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("TextTokenizationExample") \
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

    # Tokenization
    tokenizer = HuggingFaceTextTokenizer(input_col="text",
                                         output_col="tokens",
                                         name="bert-base-cased")
    outputDf = tokenizer.tokenize(df)

    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").orc(output_path)
    else:
        print("Printing results output stream")
        outputDf.printSchema()
        # root
        #  |-- id: string (nullable = true)
        #  |-- text: string (nullable = true)
        #  |-- tokens: array (nullable = true)
        #  |    |-- element: string (containsNull = true)

        outputDf.show(truncate=False)
        # +---+--------------------------+--------------------------------------------------------+
        # |id |text                      |tokens                                                  |
        # +---+--------------------------+--------------------------------------------------------+
        # |1  |Hello, y'all! How are you?|[[CLS], Hello, ,, y, ', all, !, How, are, you, ?, [SEP]]|
        # |2  |Hello to you too!         |[[CLS], Hello, to, you, too, !, [SEP]]                  |
        # |3  |I'm fine, thank you!      |[[CLS], I, ', m, fine, ,, thank, you, !, [SEP]]         |
        # +---+--------------------------+--------------------------------------------------------+

    spark.stop()
