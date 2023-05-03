import argparse
import os
import sys
import pandas as pd
import numpy as np
from PIL import Image

from typing import Iterator

from pyspark.sql.session import SparkSession
from pyspark.sql.functions import udf
from pyspark.sql.types import StructType, StructField, BinaryType, FloatType, ArrayType

from djl_spark.task.binary import BinaryPredictor
from djl_spark.util import np_util


def transform(img_data):
    img = Image.frombytes(mode='RGB', data=img_data.data, size=[img_data.width, img_data.height])

    # Resize
    img = img.resize([224, 224], resample=Image.Resampling.BICUBIC)

    # BGR to RGB
    arr = np.flip(np.asarray(img), axis=2)

    # ToTensor operation
    arr = np.expand_dims(arr, axis=0)
    arr = np.divide(arr, 255.0)
    arr = arr.transpose((0, 3, 1, 2))
    arr = np.squeeze(arr, axis=0)
    arr = np.float32(arr)
    return np_util.to_npz([arr])


def transform_udf(iterator: Iterator[pd.DataFrame]) -> Iterator[pd.DataFrame]:
    for batch in iterator:
        batch["transformed_image"] = batch.apply(transform, axis=1)
        yield batch


def softmax(data):
    arr = np_util.from_npz(data)[0]
    prob = np.exp(arr) / np.sum(np.exp(arr), axis=0)
    return prob.tolist()


def main():
    parser = argparse.ArgumentParser(description="app inputs and outputs")
    parser.add_argument("--s3_input_bucket", type=str, help="s3 input bucket")
    parser.add_argument("--s3_input_key_prefix", type=str, help="s3 input key prefix")
    parser.add_argument("--s3_output_bucket", type=str, help="s3 output bucket")
    parser.add_argument("--s3_output_key_prefix", type=str, help="s3 output key prefix")
    args = parser.parse_args()

    spark = SparkSession.builder.appName("sm-spark-djl-binary").getOrCreate()

    # Input
    df = spark.read.format("image").option("dropInvalid", True).load("s3://" + os.path.join(args.s3_input_bucket, args.s3_input_key_prefix))

    df = df.select("image.*").filter("nChannels=3")  # The model expects RGB images

    # Pre-processing
    schema = StructType(df.select("*").schema.fields + [
        StructField("transformed_image", BinaryType(), True)
    ])
    transformed_df = df.mapInPandas(transform_udf, schema)

    # Inference
    predictor = BinaryPredictor(input_col="transformed_image",
                                output_col="prediction",
                                engine="PyTorch",
                                model_url="djl://ai.djl.pytorch/resnet",
                                batchifier="stack")
    outputDf = predictor.predict(transformed_df)

    # Post-processing
    softmax_udf = udf(softmax, ArrayType(FloatType()))
    outputDf = outputDf.withColumn("probabilities", softmax_udf(outputDf['prediction']))

    outputDf = outputDf.select("origin", "probabilities")
    outputDf.write.mode("overwrite").parquet("s3://" + os.path.join(args.s3_output_bucket, args.s3_output_key_prefix))


if __name__ == "__main__":
    main()
