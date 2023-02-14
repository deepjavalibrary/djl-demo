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

import ai.djl.spark.task.text.HuggingFaceTextTokenizer

/**
 * Example to run text tokenization on Spark.
 */
object TextTokenizationExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkUtils.createSparkSession("TextTokenizationExample")

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

    // Tokenization
    val tokenizer = new HuggingFaceTextTokenizer()
      .setInputCol("text")
      .setOutputCol("tokens")
      .setName("bert-base-cased")
    val outputDf = tokenizer.tokenize(df)

    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.printSchema()
      // root
      //  |-- id: string (nullable = true)
      //  |-- text: string (nullable = true)
      //  |-- tokens: array (nullable = true)
      //  |    |-- element: string (containsNull = true)

      outputDf.show(truncate = false)
      // +---+--------------------------+--------------------------------------------------------+
      // |id |text                      |tokens                                                  |
      // +---+--------------------------+--------------------------------------------------------+
      // |1  |Hello, y'all! How are you?|[[CLS], Hello, ,, y, ', all, !, How, are, you, ?, [SEP]]|
      // |2  |Hello to you too!         |[[CLS], Hello, to, you, too, !, [SEP]]                  |
      // |3  |I'm fine, thank you!      |[[CLS], I, ', m, fine, ,, thank, you, !, [SEP]]         |
      // +---+--------------------------+--------------------------------------------------------+
    }

    spark.stop()
  }
}
