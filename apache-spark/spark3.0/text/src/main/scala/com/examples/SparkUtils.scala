package com.examples

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkUtils {

  def createSparkSession(name: String): SparkSession = {
    val spark = SparkSession.builder()
      .appName(name)
      .getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("ERROR")
    spark
  }

  def createTextDataFrame(spark: SparkSession): DataFrame = {
    val columns = Seq("id", "text")
    val data = Seq(("1", "Hello, y'all! How are you?"),
      ("2", "Hello to you too!"),
      ("3", "I'm fine, thank you!"))
    spark.createDataFrame(data).toDF(columns: _*)
  }
}
