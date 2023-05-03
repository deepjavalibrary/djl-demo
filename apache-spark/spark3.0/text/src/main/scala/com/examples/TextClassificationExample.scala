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

import ai.djl.spark.task.text.TextClassifier
import org.apache.spark.sql.SparkSession

/**
 * Example to run text classification on Spark.
 */
object TextClassificationExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("TextClassificationExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Input
    val df = spark.createDataFrame(Seq(
      (1, "Hello, y'all! How are you?"),
      (2, "Hello, my dog is cute")
    )).toDF("id", "text")

    // Text classification
    val classifier = new TextClassifier()
      .setInputCol("text")
      .setOutputCol("prediction")
      .setEngine("PyTorch")
      .setModelUrl("djl://ai.djl.huggingface.pytorch/distilbert-base-uncased-finetuned-sst-2-english")
    var outputDf = classifier.classify(df)
    outputDf.printSchema()
    // root
    //  |-- id: integer (nullable = true)
    //  |-- text: string (nullable = true)
    //  |-- prediction: struct (nullable = true)
    //  |    |-- class_names: array (nullable = true)
    //  |    |    |-- element: string (containsNull = true)
    //  |    |-- probabilities: array (nullable = true)
    //  |    |    |-- element: double (containsNull = true)
    //  |    |-- top_k: array (nullable = true)
    //  |    |    |-- element: string (containsNull = true)

    outputDf = outputDf.select("text", "prediction.top_k")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").parquet(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.show(truncate = false)
      // +--------------------------+----------------------------------------------------------------------------------------------+
      // |text                      |top_k                                                                                         |
      // +--------------------------+----------------------------------------------------------------------------------------------+
      // |Hello, y'all! How are you?|[{"class": "POSITIVE", "probability": 0.99722}, {"class": "NEGATIVE", "probability": 0.00277}]|
      // |Hello, my dog is cute     |[{"class": "POSITIVE", "probability": 0.99978}, {"class": "NEGATIVE", "probability": 0.00021}]|
      // +--------------------------+----------------------------------------------------------------------------------------------+
    }

    spark.stop()
  }
}
