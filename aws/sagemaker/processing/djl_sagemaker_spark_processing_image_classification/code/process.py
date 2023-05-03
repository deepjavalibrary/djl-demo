import argparse
import os

from pyspark.sql import SparkSession
from djl_spark.task.vision import ImageClassifier


def main():
    parser = argparse.ArgumentParser(description="app inputs and outputs")
    parser.add_argument("--s3_input_bucket", type=str, help="s3 input bucket")
    parser.add_argument("--s3_input_key_prefix", type=str, help="s3 input key prefix")
    parser.add_argument("--s3_output_bucket", type=str, help="s3 output bucket")
    parser.add_argument("--s3_output_key_prefix", type=str, help="s3 output key prefix")
    args = parser.parse_args()

    spark = SparkSession.builder.appName("sm-spark-djl-image-classification").getOrCreate()

    df = spark.read.format("image").option("dropInvalid", True).load("s3://" + os.path.join(args.s3_input_bucket, args.s3_input_key_prefix))
    df = df.select("image.*").filter("nChannels=3") # The model expects RGB images

    # Image classification
    classifier = ImageClassifier(input_cols=["origin", "height", "width", "nChannels", "mode", "data"],
                                 output_col="prediction",
                                 engine="PyTorch",
                                 model_url="djl://ai.djl.pytorch/resnet",
                                 top_k=2)
    outputDf = classifier.classify(df).select("origin", "prediction.top_k")
    outputDf.write.mode("overwrite").parquet("s3://" + os.path.join(args.s3_output_bucket, args.s3_output_key_prefix))


if __name__ == "__main__":
    main()
