/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import ai.djl.spark.task.text.TextEmbedder

/**
 * Example to run text embedding on Spark.
 */
object TextEmbeddingExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkUtils.createSparkSession("TextEmbeddingExample")

    // Input
    val df = SparkUtils.createTextDataFrame(spark)
    df.show(truncate = false)
    // +---+--------------------------+
    // |id |text                      |
    // +---+--------------------------+
    // |1  |Hello, y'all! How are you?|
    // |2  |Hello to you too!         |
    // |3  |I'm fine, thank you!      |
    // +---+--------------------------+

    // Embedding
    val embedder = new TextEmbedder()
      .setInputCol("text")
      .setOutputCol("embedding")
      .setEngine("TensorFlow")
      .setModelUrl("https://storage.googleapis.com/tfhub-modules/google/universal-sentence-encoder/4.tar.gz")
    val outputDf = embedder.embed(df)

    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.printSchema()
      // root
      //  |-- id: string (nullable = true)
      //  |-- text: string (nullable = true)
      //  |-- embedding: array (nullable = true)
      //  |    |-- element: float (containsNull = true)

      outputDf.show()
      // +---+--------------------+--------------------+
      // | id|                text|           embedding|
      // +---+--------------------+--------------------+
      // |  1|Hello, y'all! How...|[-0.05769434, -0....|
      // |  2|   Hello to you too!|[0.012077843, -0....|
      // |  3|I'm fine, thank you!|[-0.040518265, -0...|
      // +---+--------------------+--------------------+
    }

    spark.stop()
  }
}
