# DJL Spark Image Classification Binary Example

## Introduction
This folder contains a demo application built with Spark and DJL to run a group image classification task.
Note that this demo does its own pre-processing using numpy and pass in the input in .npz binary format, do
the inference, then does its own post-processing and loads result in .npz binary format.

First in the pre-processing step, this example apply transformations such as resize, then converts input to
.npz binary format.

```python
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
```

Then, we use DJL to do the inference.

```python
predictor = BinaryPredictor(input_col="transformed_image",
                            output_col="prediction",
                            engine="PyTorch",
                            model_url="djl://ai.djl.pytorch/resnet",
                            batchifier="stack")
outputDf = predictor.predict(transformed_df)
```

Finally in the post-processing step, it loads the result from .npz binary format, then apply softmax function to
convert logits to probabilities.

```python
def softmax(data):
    arr = np_util.from_npz(data)[0]
    prob = np.exp(arr) / np.sum(np.exp(arr), axis=0)
    return prob.tolist()
```

## Run the example

This example will run image classification with pretrained `resnet18` model on images in the
`s3://djl-ai/resources/demo/spark/image_classification` S3 bucket.

Use `spark-submit` to run the examples.

```
spark-submit \
    --master yarn \
    --mode cluster \
    --conf spark.executor.instances=2 \
    --conf spark.driver.memory=2g \
    --conf spark.executor.memory=6G \
    --conf spark.executor.cores=4 \
    --conf spark.driver.memory=1G \
    --conf spark.driver.cores=1 \
    image_classification_np.py
```

Refer to the [Set up EMR on EKS](../image-classification-pyspark/README.md) if you want to run this example
on EMR on EKS.

This is the expected output from console:
```
+--------------------+--------------------+
|              origin|       probabilities|
+--------------------+--------------------+
|s3://djl-ai/resou...|[1.9199406E-5, 4....|
|s3://djl-ai/resou...|[8.3182664E-7, 2....|
|s3://djl-ai/resou...|[3.0311656E-10, 3...|
+--------------------+--------------------+
```
