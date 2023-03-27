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

import ai.djl.spark.task.vision.SemanticSegmenter
import org.apache.spark.sql.SparkSession

/**
 * Example to run semantic segmentation on Spark.
 */
object SemanticSegmentationExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("SemanticSegmentationExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    var df = spark.read.format("image").option("dropInvalid", true).load("s3://djl-ai/resources/images/dog_bike_car.jpg")
    df.printSchema()
    // root
    // |-- image: struct (nullable = true)
    // |    |-- origin: string (nullable = true)
    // |    |-- height: integer (nullable = true)
    // |    |-- width: integer (nullable = true)
    // |    |-- nChannels: integer (nullable = true)
    // |    |-- mode: integer (nullable = true)
    // |    |-- data: binary (nullable = true)

    df = df.select("image.*").filter("nChannels=3") // The model expects RGB images

    val segmenter = new SemanticSegmenter()
      .setInputCols(Array("origin", "height", "width", "nChannels", "mode", "data"))
      .setOutputCol("prediction")
      .setEngine("PyTorch")
      .setModelUrl("https://mlrepo.djl.ai/model/cv/semantic_segmentation/ai/djl/pytorch/deeplabv3/0.0.1/deeplabv3.zip")
    var outputDf = segmenter.segment(df)
    outputDf.printSchema()
    // root
    //  |-- origin: string (nullable = true)
    //  |-- height: integer (nullable = true)
    //  |-- width: integer (nullable = true)
    //  |-- nChannels: integer (nullable = true)
    //  |-- mode: integer (nullable = true)
    //  |-- data: binary (nullable = true)
    //  |-- prediction: struct (nullable = true)
    //  |    |-- classes: array (nullable = true)
    //  |    |    |-- element: string (containsNull = true)
    //  |    |-- mask: array (nullable = true)
    //  |    |    |-- element: array (containsNull = true)
    //  |    |    |    |-- element: integer (containsNull = true)

    outputDf = outputDf.select("origin", "prediction.*")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.show()
      // +--------------------+--------------------+--------------------+
      // |              origin|             classes|                mask|
      // +--------------------+--------------------+--------------------+
      // |s3://djl-ai/resou...|[__background__, ...|[[0, 0, 0, 0, 0, ...|
      // +--------------------+--------------------+--------------------+
    }

    spark.stop()
  }
}
