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

import java.net.URL
import java.nio.file.{Files, Path}
import java.util

import ai.djl.{Device, Model}
import ai.djl.modality.Classifications
import ai.djl.modality.cv.transform.{Resize, ToTensor}
import ai.djl.ndarray.types.{DataType, Shape}
import ai.djl.ndarray.{NDList, NDManager}
import ai.djl.repository.zoo.{Criteria, ModelZoo, ZooModel}
import ai.djl.training.util.{DownloadUtils, ProgressBar}
import ai.djl.translate.{Batchifier, Pipeline, Translator, TranslatorContext}
import ai.djl.util.{Utils, ZipUtils}
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.ml.image.ImageSchema
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Encoders, Row, SparkSession}


/**
 * Example to run image classification on Spark.
 */
object ImageClassificationExample {

  def loadModel(device : Device): ZooModel[Row, Classifications] = {
    val modelUrl = "https://alpha-djl-demos.s3.amazonaws.com/model/djl-blockrunner/pytorch_resnet18.zip?model_name=traced_resnet18"
    val criteria = Criteria.builder
      .setTypes(classOf[Row], classOf[Classifications])
      .optModelUrls(modelUrl)
      .optTranslator(new MyTranslator())
      .optProgress(new ProgressBar)
      .optDevice(device)
      .build()
    ModelZoo.loadModel(criteria)
  }

  // Translator: a class used to do preprocessing and post processing
  class MyTranslator extends Translator[Row, Classifications] {

    private var classes: java.util.List[String] = new util.ArrayList[String]()
    private val pipeline: Pipeline = new Pipeline()
      .add(new Resize(224, 224))
      .add(new ToTensor())

    override def prepare(manager: NDManager, model: Model): Unit = {
        classes = Utils.readLines(model.getArtifact("synset.txt").openStream())
      }

    override def processInput(ctx: TranslatorContext, row: Row): NDList = {

      val height = ImageSchema.getHeight(row)
      val width = ImageSchema.getWidth(row)
      val channel = ImageSchema.getNChannels(row)
      var image = ctx.getNDManager.create(ImageSchema.getData(row), new Shape(height, width, channel)).toType(DataType.UINT8, true)
      // BGR to RGB
      image = image.flip(2)
      pipeline.transform(new NDList(image))
    }

    // Deal with the output.，NDList contains output result, usually one or more NDArray(s).
    override def processOutput(ctx: TranslatorContext, list: NDList): Classifications = {
      var probabilitiesNd = list.singletonOrThrow
      probabilitiesNd = probabilitiesNd.softmax(0)
      new Classifications(classes, probabilitiesNd)
    }

    override def getBatchifier: Batchifier = Batchifier.STACK
  }

  def downloadImages(outputPath : Path) : String = {
    val url = "https://alpha-djl-demos.s3.amazonaws.com/spark-demo/images.zip"
    DownloadUtils.download(new URL(url), outputPath.resolve("images.zip"), new ProgressBar())
    ZipUtils.unzip(Files.newInputStream(outputPath.resolve("images.zip")), outputPath)
    outputPath.toAbsolutePath.toString
  }

  def main(args: Array[String]) {

    // download images
    val imagePath = downloadImages(Files.createTempDirectory("images"))
    // Spark configuration
    val spark = SparkSession.builder()
      .appName("Image Classification")
      .config(new SparkConf())
      .getOrCreate()

    spark.conf.getAll.foreach(pair => println(pair._1 + ":" + pair._2))

    val df = spark.read.format("image").option("dropInvalid", true).load(imagePath)
    println(df.select("image.origin", "image.width", "image.height").show(truncate=false))

    val result = df.select(col("image.*")).mapPartitions(partition => {
      val context = TaskContext.get()
      val gpu = context.resources()("gpu").addresses(0)
      val model = loadModel(Device.gpu(gpu.toInt))
      val predictor = model.newPredictor()
      partition.map(row => {
        // image data stored as HWC format
        predictor.predict(row).toString
      })
    })(Encoders.STRING)
    println(result.collect().mkString("\n"))
  }
}
