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

import ai.djl.spark.task.vision.ImageClassifier
import org.apache.spark.sql.SparkSession

/**
 * Example to run image classification on Spark.
 */
object ImageClassificationExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("ImageClassificationExample")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    var df = spark.read.format("image").option("dropInvalid", true).load("s3://djl-ai/resources/demo/spark/image_classification/")
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

    val classifier = new ImageClassifier()
      .setInputCols(Array("origin", "height", "width", "nChannels", "mode", "data"))
      .setOutputCol("prediction")
      .setEngine("PyTorch")
      .setModelUrl("djl://ai.djl.pytorch/resnet")
      .setTopK(2)
    var outputDf = classifier.classify(df)
    outputDf.printSchema()
    // root
    //  |-- origin: string (nullable = true)
    //  |-- height: integer (nullable = true)
    //  |-- width: integer (nullable = true)
    //  |-- nChannels: integer (nullable = true)
    //  |-- mode: integer (nullable = true)
    //  |-- data: binary (nullable = true)
    //  |-- prediction: struct (nullable = true)
    //  |    |-- class_names: array (nullable = true)
    //  |    |    |-- element: string (containsNull = true)
    //  |    |-- probabilities: array (nullable = true)
    //  |    |    |-- element: double (containsNull = true)
    //  |    |-- topK: map (nullable = true)
    //  |    |    |-- key: string
    //  |    |    |-- value: double (valueContainsNull = true)

    outputDf = outputDf.select("origin", "prediction.topK")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.show(truncate = false)
      // +-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
      // |origin                                                       |topK                                                                                                                                                           |
      // +-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
      // |s3://djl-ai/resources/demo/spark/image_classification/car.jpg|{n03770679 minivan -> 0.8499245047569275, n02814533 beach wagon, station wagon, wagon, estate car, beach waggon, station waggon, waggon -> 0.04071871191263199}|
      // |s3://djl-ai/resources/demo/spark/image_classification/dog.jpg|{n02085936 Maltese dog, Maltese terrier, Maltese -> 0.7925896644592285, n02113624 toy poodle -> 0.11378670483827591}                                           |
      // |s3://djl-ai/resources/demo/spark/image_classification/cat.jpg|{n02123394 Persian cat -> 0.9232246279716492, n02127052 lynx, catamount -> 0.05277140066027641}                                                                |
      // +-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
    }

    spark.stop()
  }
}
