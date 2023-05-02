import argparse
import os

from pyspark.sql import SparkSession
from djl_spark.task.audio import WhisperSpeechRecognizer


def main():
    parser = argparse.ArgumentParser(description="app inputs and outputs")
    parser.add_argument("--s3_input_bucket", type=str, help="s3 input bucket")
    parser.add_argument("--s3_input_key_prefix", type=str, help="s3 input key prefix")
    parser.add_argument("--s3_output_bucket", type=str, help="s3 output bucket")
    parser.add_argument("--s3_output_key_prefix", type=str, help="s3 output key prefix")
    args = parser.parse_args()

    spark = SparkSession.builder.appName("sm-spark-djl-whisper-speech-reconition").getOrCreate()

    # Input
    df = spark.read.format("binaryFile").load("s3://" + os.path.join(args.s3_input_bucket, args.s3_input_key_prefix))

    # Speech recognition
    recognizer = WhisperSpeechRecognizer(input_col="content",
                                         output_col="prediction",
                                         hf_model_id="openai/whisper-base",
                                         batch_size=30)
    generate_kwargs = {"task":"transcribe", "language":"<|en|>"}
    outputDf = recognizer.recognize(df, generate_kwargs=generate_kwargs, return_timestamps=False).select("path", "prediction")
    outputDf.write.mode("overwrite").csv("s3://" + os.path.join(args.s3_output_bucket, args.s3_output_key_prefix))


if __name__ == "__main__":
    main()
