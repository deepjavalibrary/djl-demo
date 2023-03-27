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

import ai.djl.spark.task.audio.SpeechRecognizer
import org.apache.spark.sql.SparkSession

/**
 * Example to run speech recognition on Spark.
 */
object SpeechRecognitionExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("SpeechRecognitionExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Input
    val df = spark.read.format("binaryFile").load("s3://djl-ai/resources/audios/speech.wav")
    df.printSchema()
    // root
    //  |-- path: string (nullable = true)
    //  |-- modificationTime: timestamp (nullable = true)
    //  |-- length: long (nullable = true)
    //  |-- content: binary (nullable = true)

    // Speech recognition
    val recognizer = new SpeechRecognizer()
      .setInputCol("content")
      .setOutputCol("prediction")
      .setEngine("PyTorch")
      .setModelUrl("https://resources.djl.ai/test-models/pytorch/wav2vec2.zip")
    val outputDf = recognizer.recognize(df).select("path", "prediction")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.show(truncate = false)
      // +---------------------------------------+---------------------------------------------------------------------------------------------------------------------+
      // |path                                   |prediction                                                                                                           |
      // +---------------------------------------+---------------------------------------------------------------------------------------------------------------------+
      // |s3://djl-ai/resources/audios/speech.wav|THE NEAREST SAID THE DISTRICT DOCTOR IS A GOOD ITALIAN ABBE WHO LIVES NEXT DOOR TO YOU SHALL I CALL ON HIM AS I PASS |
      // +---------------------------------------+---------------------------------------------------------------------------------------------------------------------+
    }

    spark.stop()
  }
}
