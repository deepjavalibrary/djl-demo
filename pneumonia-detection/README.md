# Pneumonia Detection

This is an example to demonstrate how to use [Deep Java Library](http://djl.ai) to detect Pneumonia based on Chest X-ray images.

For more details, please follow [Chest X-Ray Images (Pneumonia)](https://www.kaggle.com/paultimothymooney/chest-xray-pneumonia) on Kaggle 
and [this Kernel](https://www.kaggle.com/aakashnain/beating-everything-with-depthwise-convolution).

You can obtain the training script from the above Kernel. We will use the model and DJL for prediction.

## Setup
run the following command to build the project:

`./gradlew build`

## Run prediction on images

### Prepare images

You can download some of the X-ray images for testing dataset on [kaggle](https://www.kaggle.com/paultimothymooney/chest-xray-pneumonia).
We will use a default image for prediction if no input image is specified.

### Prepare trained model

You can find a trained model [here](https://djl-ai.s3.amazonaws.com/resources/demo/pneumonia-detection-model/saved_model.zip),
download and unzip it.

### Run prediction

run the following command:

```
mkdir models
cd models
curl https://djl-ai.s3.amazonaws.com/resources/demo/pneumonia-detection-model/saved_model.zip | jar xv
cd ..

./gradlew run -Dai.djl.repository.zoo.location=models/saved_model
```


## Reference:

1. [Detecting Pneumonia based on chest X-ray images using Depthwise Convolution](https://www.kaggle.com/aakashnain/beating-everything-with-depthwise-convolution)
2. [DJL TensorFlow Engine](https://github.com/awslabs/djl/tree/master/tensorflow/tensorflow-engine)
3. [DJL TensorFlow Documentation](https://github.com/awslabs/djl/tree/master/docs/tensorflow)
