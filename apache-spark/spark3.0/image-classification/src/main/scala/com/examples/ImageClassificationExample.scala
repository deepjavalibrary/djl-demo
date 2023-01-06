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
import ai.djl.spark.SparkTransformer
import ai.djl.spark.translator.SparkImageClassificationTranslator
import org.apache.spark.sql.SparkSession

/**
 * Example to run image classification on Spark.
 */
object ImageClassificationExample {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("Image Classification")
      .getOrCreate()

    val df = spark.read.format("image").option("dropInvalid", true).load("s3://alpha-djl-demos/temp/images")
    df.select("image.origin", "image.width", "image.height").show(truncate = false)

    val transformer = new SparkTransformer[Classifications]()
      .setInputCol("image.*")
      .setOutputCol("value")
      .setModelUrl("https://alpha-djl-demos.s3.amazonaws.com/model/djl-blockrunner/pytorch_resnet18.zip?model_name=traced_resnet18")
      .setOutputClass(classOf[Classifications])
      .setTranslator(new SparkImageClassificationTranslator())
    val outputDf = transformer.transform(df)
    outputDf.show(truncate = false)

    spark.stop()
  }
}
