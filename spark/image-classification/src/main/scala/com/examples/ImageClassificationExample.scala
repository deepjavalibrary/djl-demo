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

import ai.djl.modality.Classifications
import ai.djl.modality.cv.transform.{Resize, ToTensor}
import ai.djl.modality.cv.translator.ImageClassificationTranslator
import ai.djl.modality.cv.{Image, ImageFactory}
import ai.djl.repository.zoo.{Criteria, ModelZoo}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Example to run image classification on Spark.
 */
object ImageClassificationExample {
  def main(args: Array[String]) {

    // Spark configuration
    val conf = new SparkConf()
      .setAppName("Simple Image Classification")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val partitions = sc.binaryFiles("images/*")
    // Start assign work for each worker node
    val result = partitions.mapPartitions(partition => {
      val modelUrl = "https://alpha-djl-demos.s3.amazonaws.com/model/djl-blockrunner/pytorch_resnet18.zip?model_name=traced_resnet18"
      // switch to MXNet and TF by uncomment one of the two lines
      // val modelUrl = "https://alpha-djl-demos.s3.amazonaws.com/model/djl-blockrunner/mxnet_resnet18.zip?model_name=resnet18_v1"
      // val modelUrl = "https://alpha-djl-demos.s3.amazonaws.com/model/djl-blockrunner/tensorflow_MobileNet.zip"
      val criteria = Criteria.builder
        .setTypes(classOf[Image], classOf[Classifications])
        .optModelUrls(modelUrl)
        .optTranslator(ImageClassificationTranslator
          .builder.addTransform(new Resize(224, 224))
          .addTransform(new ToTensor)
          .optApplySoftmax(true).build)
        .build
      val model = ModelZoo.loadModel(criteria)
      val predictor = model.newPredictor()
      partition.map(streamData => {
        val img = ImageFactory.getInstance().fromInputStream(streamData._2.open())
        predictor.predict(img).toString
      })
    })
    // The real execution started here
    result.saveAsTextFile("out/spark_output")
    result.foreach(print)
    println("OK")
  }
}
