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
from djl_spark.task.text import TextEncoder, TextDecoder


if __name__ == "__main__":
    """
        Usage: text_encoding.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("TextEncodingExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.createDataFrame(
        [
            (1, "Hello, y'all! How are you?"),
            (2, "Hello to you too!"),
            (3, "I'm fine, thank you!")
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

    # Encoding
    encoder = TextEncoder(input_col="text",
                          output_col="encoded",
                          hf_model_id="bert-base-cased")
    encDf = encoder.encode(df)
    encDf.printSchema()
    # root
    #  |-- id: long (nullable = true)
    #  |-- text: string (nullable = true)
    #  |-- encoded: struct (nullable = true)
    #  |    |-- ids: array (nullable = true)
    #  |    |    |-- element: long (containsNull = true)
    #  |    |-- type_ids: array (nullable = true)
    #  |    |    |-- element: long (containsNull = true)
    #  |    |-- attention_mask: array (nullable = true)
    #  |    |    |-- element: long (containsNull = true)

    # Decoding
    encDf = encDf.select("id", "text", "encoded.*")
    decoder = TextDecoder(input_col="ids",
                          output_col="decoded",
                          hf_model_id="bert-base-cased")
    decDf = decoder.decode(encDf)
    decDf.printSchema()
    # root
    #  |-- id: long (nullable = true)
    #  |-- text: string (nullable = true)
    #  |-- ids: array (nullable = true)
    #  |    |-- element: long (containsNull = true)
    #  |-- type_ids: array (nullable = true)
    #  |    |-- element: long (containsNull = true)
    #  |-- attention_mask: array (nullable = true)
    #  |    |-- element: long (containsNull = true)
    #  |-- decoded: string (nullable = true)

    decDf = decDf.select("id", "text", "ids", "decoded")
    if output_path:
        print("Saving results S3 path: " + output_path)
        decDf.write.mode("overwrite").parquet(output_path)
    else:
        print("Printing results output stream")
        decDf.show(truncate=False)
        # +---+--------------------------+-----------------------------------------------------------------+----------------------------------------+
        # |id |text                      |ids                                                              |decoded                                 |
        # +---+--------------------------+-----------------------------------------------------------------+----------------------------------------+
        # |1  |Hello, y'all! How are you?|[101, 8667, 117, 194, 112, 1155, 106, 1731, 1132, 1128, 136, 102]|[CLS] Hello, y ' all! How are you? [SEP]|
        # |2  |Hello to you too!         |[101, 8667, 1106, 1128, 1315, 106, 102]                          |[CLS] Hello to you too! [SEP]           |
        # |3  |I'm fine, thank you!      |[101, 146, 112, 182, 2503, 117, 6243, 1128, 106, 102]            |[CLS] I ' m fine, thank you! [SEP]      |
        # +---+--------------------------+-----------------------------------------------------------------+----------------------------------------+

    spark.stop()
