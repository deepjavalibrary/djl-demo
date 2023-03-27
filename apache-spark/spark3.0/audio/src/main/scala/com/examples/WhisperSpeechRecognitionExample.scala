/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.examples

import ai.djl.spark.task.audio.WhisperSpeechRecognizer
import org.apache.spark.sql.SparkSession

/**
 * Example to run speech recognition with Whisper model on Spark.
 */
object WhisperSpeechRecognitionExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("WhisperSpeechRecognitionExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Input
    val df = spark.read.format("binaryFile").load("s3://djl-ai/resources/audios/jfk.flac")
    df.printSchema()
    // root
    //  |-- path: string (nullable = true)
    //  |-- modificationTime: timestamp (nullable = true)
    //  |-- length: long (nullable = true)
    //  |-- content: binary (nullable = true)

    // Speech recognition
    val recognizer = new WhisperSpeechRecognizer()
      .setInputCol("content")
      .setOutputCol("prediction")
      .setEngine("PyTorch")
      .setModelUrl("https://resources.djl.ai/demo/pytorch/whisper/whisper_en.zip")
      .setChannels(1)
      .setSampleRate(16000)
      .setSampleFormat(1) // AV_SAMPLE_FMT_S16, signed 16 bits
    val outputDf = recognizer.recognize(df).select("path", "prediction")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.show(truncate = false)
      // +-------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
      // |path                                 |prediction                                                                                                                                                                             |
      // +-------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
      // |s3://djl-ai/resources/audios/jfk.flac|<|startoftranscript|> <|en|> <|transcribe|> <|notimestamps|> And so my fellow Americans ask not what your country can do for you , ask what you can do for your country . <|endoftext|>|
      // +-------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
    }

    spark.stop()
  }
}
