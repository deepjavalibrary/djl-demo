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

import ai.djl.spark.task.text.TextEmbedder
import org.apache.spark.sql.SparkSession

/**
 * Example to run text embedding on Spark.
 */
object TextEmbeddingExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("TextEmbeddingExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Input
    val df = spark.createDataFrame(Seq(
      (1, "This is an example sentence"),
      (2, "Each sentence is converted")
    )).toDF("id", "text")

    // Embedding
    val embedder = new TextEmbedder()
      .setInputCol("text")
      .setOutputCol("embedding")
      .setEngine("PyTorch")
      .setModelUrl("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
    val outputDf = embedder.embed(df)

    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").parquet(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.printSchema()
      // root
      //  |-- id: integer (nullable = true)
      //  |-- text: string (nullable = true)
      //  |-- embedding: array (nullable = true)
      //  |    |-- element: float (containsNull = true)

      outputDf.show()
      // +---+--------------------+--------------------+
      // | id|                text|           embedding|
      // +---+--------------------+--------------------+
      // |  1|This is an exampl...|[0.0676569, 0.063...|
      // |  2|Each sentence is ...|[0.08643859, 0.10...|
      // +---+--------------------+--------------------+
    }

    spark.stop()
  }
}
