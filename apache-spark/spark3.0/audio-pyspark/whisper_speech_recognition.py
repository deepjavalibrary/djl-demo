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
from djl_spark.task.audio import WhisperSpeechRecognizer


if __name__ == "__main__":
    """
        Usage: whisper_speech_recognition.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("WhisperSpeechRecognitionExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    # Input
    df = spark.read.format("binaryFile").load("s3://djl-ai/resources/audios/jfk.flac")
    df.printSchema()
    # root
    #  |-- path: string (nullable = true)
    #  |-- modificationTime: timestamp (nullable = true)
    #  |-- length: long (nullable = true)
    #  |-- content: binary (nullable = true)

    # Speech recognition
    recognizer = WhisperSpeechRecognizer(input_col="content",
                                         output_col="prediction",
                                         hf_model_id="openai/whisper-base",
                                         batch_size=30)
    generate_kwargs = {"task":"transcribe", "language":"<|en|>"}
    outputDf = recognizer.recognize(df, generate_kwargs=generate_kwargs, return_timestamps=False).select("path", "prediction")

    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").csv(output_path)
    else:
        print("Printing results output stream")
        outputDf.show(truncate=False)
        # +-------------------------------------+------------------------------------------------------------------------------------------------------------+
        # |path                                 |prediction                                                                                                  |
        # +-------------------------------------+------------------------------------------------------------------------------------------------------------+
        # |s3://djl-ai/resources/audios/jfk.flac| And so my fellow Americans, ask not what your country can do for you, ask what you can do for your country.|
        # +-------------------------------------+------------------------------------------------------------------------------------------------------------+

    spark.stop()
