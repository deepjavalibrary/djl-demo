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

import ai.djl.spark.task.text.{HuggingFaceTextDecoder, HuggingFaceTextEncoder}
import org.apache.spark.sql.SparkSession

/**
 * Example to run text encoding / decoding on Spark.
 */
object TextEncodingExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("TextEncodingExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Input
    val df = spark.createDataFrame(Seq(
      (1, "Hello, y'all! How are you?"),
      (2, "Hello to you too!"),
      (3, "I'm fine, thank you!")
    )).toDF("id", "text")

    // Encoding
    val encoder = new HuggingFaceTextEncoder()
      .setInputCol("text")
      .setOutputCol("encoded")
      .setTokenizer("bert-base-cased")
    var encDf = encoder.encode(df)
    encDf.printSchema()
    // root
    //  |-- id: string (nullable = true)
    //  |-- text: string (nullable = true)
    //  |-- encoded: struct (nullable = true)
    //  |    |-- ids: array (nullable = true)
    //  |    |    |-- element: long (containsNull = true)
    //  |    |-- type_ids: array (nullable = true)
    //  |    |    |-- element: long (containsNull = true)
    //  |    |-- attention_mask: array (nullable = true)
    //  |    |    |-- element: long (containsNull = true)

    // Decoding
    encDf = encDf.select("id", "text", "encoded.*")
    val decoder = new HuggingFaceTextDecoder()
      .setInputCol("ids")
      .setOutputCol("decoded")
      .setTokenizer("bert-base-cased")
    var decDf = decoder.decode(encDf)
    decDf.printSchema()
    // root
    //  |-- id: string (nullable = true)
    //  |-- text: string (nullable = true)
    //  |-- ids: array (nullable = true)
    //  |    |-- element: long (containsNull = true)
    //  |-- type_ids: array (nullable = true)
    //  |    |-- element: long (containsNull = true)
    //  |-- attention_mask: array (nullable = true)
    //  |    |-- element: long (containsNull = true)
    //  |-- decoded: string (nullable = true)

    decDf = decDf.select("id", "text", "ids", "decoded")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      decDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      decDf.show(truncate = false)
      // +---+--------------------------+-----------------------------------------------------------------+----------------------------------------+
      // |id |text                      |ids                                                              |decoded                                 |
      // +---+--------------------------+-----------------------------------------------------------------+----------------------------------------+
      // |1  |Hello, y'all! How are you?|[101, 8667, 117, 194, 112, 1155, 106, 1731, 1132, 1128, 136, 102]|[CLS] Hello, y ' all! How are you? [SEP]|
      // |2  |Hello to you too!         |[101, 8667, 1106, 1128, 1315, 106, 102]                          |[CLS] Hello to you too! [SEP]           |
      // |3  |I'm fine, thank you!      |[101, 146, 112, 182, 2503, 117, 6243, 1128, 106, 102]            |[CLS] I ' m fine, thank you! [SEP]      |
      // +---+--------------------------+-----------------------------------------------------------------+----------------------------------------+
    }

    spark.stop()
  }
}
