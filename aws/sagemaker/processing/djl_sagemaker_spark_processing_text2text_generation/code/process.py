import argparse
import os

from pyspark.sql import SparkSession
from djl_spark.task.text import Text2TextGenerator


def main():
    parser = argparse.ArgumentParser(description="app inputs and outputs")
    parser.add_argument("--s3_input_bucket", type=str, help="s3 input bucket")
    parser.add_argument("--s3_input_key_prefix", type=str, help="s3 input key prefix")
    parser.add_argument("--s3_output_bucket", type=str, help="s3 output bucket")
    parser.add_argument("--s3_output_key_prefix", type=str, help="s3 output key prefix")
    args = parser.parse_args()

    spark = SparkSession.builder.appName("sm-spark-djl-text2text-gen").getOrCreate()

    df = spark.read.option("header","true").csv("s3://" + os.path.join(args.s3_input_bucket, args.s3_input_key_prefix))

    # Text2Text generation using Flan-Alpaca model
    generator = Text2TextGenerator(input_col="instruction",
                                   output_col="prediction",
                                   engine="PyTorch",
                                   model_name="declare-lab/flan-alpaca-base")
    outputDf = generator.generate(df, do_sample=True, max_length=128)
    outputDf.write.mode("overwrite").option("header","true").csv("s3://" + os.path.join(args.s3_output_bucket, args.s3_output_key_prefix))


if __name__ == "__main__":
    main()
