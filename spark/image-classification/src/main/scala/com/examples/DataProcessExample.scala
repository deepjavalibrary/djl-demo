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

import java.nio.file.Paths

import ai.djl.Model
import ai.djl.ndarray.{NDArrays, NDList}
import ai.djl.ndarray.types.{DataType, Shape}
import ai.djl.training.util.DownloadUtils
import ai.djl.training.util.ProgressBar
import ai.djl.translate.{Batchifier, Translator, TranslatorContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Scratch example to load model from path and do customized operations for the input and output.
 */
object DataProcessExample {

  private lazy val model: Model = loadModel()

  def downloadModel(): Unit = {
    DownloadUtils.download("https://djl-ai.s3.amazonaws.com/mlrepo/model/cv/image_classification/ai/djl/pytorch/resnet/0.0.1/traced_resnet18.pt.gz",
      "out/resnet18.pt", new ProgressBar())
  }

  def loadModel(): Model = {
    downloadModel()
    // initialize engine
    val model = Model.newInstance()
    // load torchscript traced model
    model.load(Paths.get("out/resnet18.pt"))
    model
  }

  // Translator: a class used to do preprocessing and post processing
  class MyTranslator extends Translator[Array[Int], String] {
    // Deal with the input. NDList (List of NDArray）will passed in the model for inference
    // For this model, input shape is (batch size, 3, 224, 224)
    override def processInput(ctx: TranslatorContext, input: Array[Int]): NDList = {
      // Get NDManager
      val manager = ctx.getNDManager
      // Generate NDArray
      val ndArray = manager.create(input)
      val totalLength = 3 * 224 * 224
      // NDArray to be concatenate
      val ndToBeConcat = manager.ones(new Shape(totalLength - input.length), DataType.INT32)
      // concatenation and reshape operation
      var result = NDArrays.concat(new NDList(ndArray, ndToBeConcat)).reshape(new Shape(3, 224, 224))
      // change data type to float32 as needed
      result = result.toType(DataType.FLOAT32, true)
      // package and send them out
      new NDList(result)
    }

    // Deal with the output.，NDList contains output result, usually one or more NDArray(s).
    override def processOutput(ctx: TranslatorContext, list: NDList): String = {
      // result contains inference result
      val result = list.singletonOrThrow()
      "This is the output"
    }
  }

  def main(args: Array[String]) {

    // Spark configuration
    val conf = new SparkConf()
      .setAppName("Simple Image Classification")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val partitions = sc.binaryFiles("images/*")
    // Start assign work for each worker node
    val result = partitions.mapPartitions( partition => {
      // We need to make sure predictor are spawned on a executor basis to save memory
      val predictor = model.newPredictor(new MyTranslator)
      partition.map(streamData => {
        val data = Array(1, 2, 3)
        predictor.predict(data)
      })
    })
    // The real execution started here
    result.saveAsTextFile("out/spark_output")
    result.foreach(print)
    println("OK")
  }
}
