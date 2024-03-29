import argparse
import os

from pyspark.sql import SparkSession
from djl_spark.task.text import TextClassifier


def main():
    parser = argparse.ArgumentParser(description="app inputs and outputs")
    parser.add_argument("--s3_input_bucket", type=str, help="s3 input bucket")
    parser.add_argument("--s3_input_key_prefix", type=str, help="s3 input key prefix")
    parser.add_argument("--s3_output_bucket", type=str, help="s3 output bucket")
    parser.add_argument("--s3_output_key_prefix", type=str, help="s3 output key prefix")
    args = parser.parse_args()

    spark = SparkSession.builder.appName("sm-spark-djl-text-classification").getOrCreate()

    df = spark.read.json("s3://" + os.path.join(args.s3_input_bucket, args.s3_input_key_prefix))

    # Text classification
    classifier = TextClassifier(input_col="inputs",
                                output_col="prediction",
                                engine="PyTorch",
                                model_url="djl://ai.djl.huggingface.pytorch/distilbert-base-uncased-finetuned-sst-2-english")
    outputDf = classifier.classify(df).select("text", "prediction.top_k")
    outputDf.write.mode("overwrite").parquet("s3://" + os.path.join(args.s3_output_bucket, args.s3_output_key_prefix))


if __name__ == "__main__":
    main()
