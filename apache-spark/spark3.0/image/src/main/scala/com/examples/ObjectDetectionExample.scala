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

    val detector = new ObjectDetector()
      .setInputCols(Array("origin", "height", "width", "nChannels", "mode", "data"))
      .setOutputCol("prediction")
      .setEngine("PyTorch")
      .setModelUrl("https://mlrepo.djl.ai/model/cv/object_detection/ai/djl/pytorch/yolov5s/0.0.1/yolov5s.zip")
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
    //  |    |-- boundingBoxes: array (nullable = true)
    //  |    |    |-- element: string (containsNull = true)

    outputDf = outputDf.select("origin", "prediction.*")
    if (outputPath != null) {
      println("Saving results S3 path: " + outputPath)
      outputDf.write.mode("overwrite").orc(outputPath)
    } else {
      println("Printing results to output stream")
      outputDf.show(truncate = false)
      // +---------------------------------------------+-----------+----------------------------------------+-------------------------------------------------------------------------------------------------------------+
      // |origin                                       |class_names|probabilities                           |boundingBoxes                                                                                                |
      // +---------------------------------------------+-----------+----------------------------------------+-------------------------------------------------------------------------------------------------------------+
      // |s3://djl-ai/resources/images/dog_bike_car.jpg|[car, dog] |[0.6604642271995544, 0.9015125036239624]|[[x=392.622, y=85.052, width=181.403, height=105.517], [x=108.948, y=240.220, width=150.455, height=370.261]]|
      // +---------------------------------------------+-----------+----------------------------------------+-------------------------------------------------------------------------------------------------------------+
    }

    spark.stop()
  }
}
