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

import ai.djl.spark.task.text.QuestionAnswerer
import org.apache.spark.sql.SparkSession

/**
 * Example to run question answering on Spark.
 */
object QuestionAnsweringExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("QuestionAnsweringExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Input
    val df = spark.createDataFrame(Seq(
      (1, "Who was Jim Henson?", "Jim Henson was a puppeteer"),
      (2, "Where do I live?", "My name is Wolfgang and I live in Berlin")
    )).toDF("id", "question", "context")

    // Question answering
    val answerer = new QuestionAnswerer()
      .setInputCols(Array("question", "context"))
      .setOutputCol("answer")
      .setEngine("PyTorch")
      .setModelUrl("djl://ai.djl.huggingface.pytorch/deepset/bert-base-cased-squad2")
    val outputDf = answerer.answer(df)

    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").csv(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.printSchema()
      // root
      //  |-- id: integer (nullable = false)
      //  |-- question: string (nullable = true)
      //  |-- context: string (nullable = true)
      //  |-- answer: string (nullable = true)

      outputDf.show(truncate = false)
      // +---+-------------------+----------------------------------------+-----------+
      // |id |question           |paragraph                               |answer     |
      // +---+-------------------+----------------------------------------+-----------+
      // |1  |Who was Jim Henson?|Jim Henson was a puppeteer              |a puppeteer|
      // |2  |Where do I live?   |My name is Wolfgang and I live in Berlin|Berlin     |
      // +---+-------------------+----------------------------------------+-----------+
    }

    spark.stop()
  }
}
