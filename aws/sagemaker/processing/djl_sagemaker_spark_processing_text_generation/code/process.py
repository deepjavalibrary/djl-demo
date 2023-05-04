import argparse
import os

from pyspark.sql import SparkSession
from pyspark.sql.functions import col, substring_index
from djl_spark.task.text import TextGenerator


def main():
    parser = argparse.ArgumentParser(description="app inputs and outputs")
    parser.add_argument("--s3_input_bucket", type=str, help="s3 input bucket")
    parser.add_argument("--s3_input_key_prefix", type=str, help="s3 input key prefix")
    parser.add_argument("--s3_output_bucket", type=str, help="s3 output bucket")
    parser.add_argument("--s3_output_key_prefix", type=str, help="s3 output key prefix")
    args = parser.parse_args()

    spark = SparkSession.builder.appName("sm-spark-djl-text-generation").getOrCreate()

    # Input
    df = spark.read.json("s3://" + os.path.join(args.s3_input_bucket, args.s3_input_key_prefix))

    # Truncate the input to 8 words to make the input shorter than max_length 30
    df = df.select(substring_index(col("inputs"), " ", 8).alias("inputs"))

    # Text generation
    generator = TextGenerator(input_col="inputs",
                              output_col="prediction",
                              hf_model_id="facebook/opt-125m")
    outputDf = generator.generate(df, do_sample=True, max_length=30)
    outputDf.write.mode("overwrite").parquet("s3://" + os.path.join(args.s3_output_bucket, args.s3_output_key_prefix))


if __name__ == "__main__":
    main()
