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

import java.awt.image.BufferedImage

import ai.djl.Application
import ai.djl.modality.Classifications
import ai.djl.repository.zoo.{Criteria, ModelZoo}
import ai.djl.training.util.ProgressBar
import javax.imageio.ImageIO
import org.apache.spark.{SparkConf, SparkContext}

object Example {
  def main(args: Array[String]) {

    // Spark configuration
    val conf = new SparkConf()
      .setAppName("Simple Image Classification")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val partitions = sc.binaryFiles("images/*")
    // Start assign work for each worker node
    val result = partitions.mapPartitions( partition => {
      val criteria = Criteria.builder
        .optApplication(Application.CV.IMAGE_CLASSIFICATION)
        .setTypes(classOf[BufferedImage], classOf[Classifications])
        .optFilter("dataset", "imagenet")
        .optFilter("layers", "18")
        .optProgress(new ProgressBar)
        .build
      val model = ModelZoo.loadModel(criteria)
      val predictor = model.newPredictor()
      val data = partition.map(streamData => {
        val img = ImageIO.read(streamData._2.open())
        predictor.predict(img).toString
      }).toList // toList is a workaround for lazy evaluation
      predictor.close()
      model.close()
      data.toIterator
    })
    // The real execution started here
    result.saveAsTextFile("out/spark_output")
    result.foreach(print)
    println("OK")
  }
}
