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

import ai.djl.spark.task.vision.ObjectDetector
import org.apache.spark.sql.SparkSession

/**
 * Example to run object detection on Spark.
 */
object ObjectDetectionExample {

  def main(args: Array[String]): Unit = {
    val outputPath: String = if (args.length > 0) args(0) else null
    val spark = SparkSession.builder()
      .appName("ObjectDetectionExample")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    val df = spark.read.format("image").option("dropInvalid", true).load("s3://djl-ai/resources/images/dog_bike_car.jpg")
    df.printSchema()
    // root
    // |-- image: struct (nullable = true)
    // |    |-- origin: string (nullable = true)
    // |    |-- height: integer (nullable = true)
    // |    |-- width: integer (nullable = true)
    // |    |-- nChannels: integer (nullable = true)
    // |    |-- mode: integer (nullable = true)
    // |    |-- data: binary (nullable = true)

    // Object detection
    val detector = new ObjectDetector()
      .setInputCols(Array("origin", "height", "width", "nChannels", "mode", "data"))
      .setOutputCol("prediction")
      .setEngine("PyTorch")
      .setModelUrl("djl://ai.djl.pytorch/yolov5s")
    var outputDf = detector.detect(df)
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
    //  |    |-- bounding_boxes: array (nullable = true)
    //  |    |    |-- element: string (containsNull = true)

    outputDf = outputDf.select("origin", "prediction.bounding_boxes")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").parquet(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.show(truncate = false)
      // +---------------------------------------------+--------------------------------------------------------------------------------------------------------------+
      // |origin                                       |bounding_boxes                                                                                                |
      // +---------------------------------------------+--------------------------------------------------------------------------------------------------------------+
      // |s3://djl-ai/resources/images/dog_bike_car.jpg|[{"x"=0.613, "y"=0.133, "width"=0.283, "height"=0.165}, {"x"=0.170, "y"=0.375, "width"=0.235, "height"=0.579}]|
      // +---------------------------------------------+--------------------------------------------------------------------------------------------------------------+
    }

    spark.stop()
  }
}
