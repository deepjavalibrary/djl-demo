# Covid19 Detection

This is an example to demonstrate how to use [Deep Java Library](djl.ai) to detect COVID-19 based on X-ray images.

For more details, please follow this [blog post](https://www.pyimagesearch.com/2020/03/16/detecting-covid-19-in-x-ray-images-with-keras-tensorflow-and-deep-learning/).

You can obtain the training script and a trained Keras model from the above blog post. We will use the model and DJL for prediction.

## Setup
run the following command to build the project:

`./gradlew build`

## Run prediction on images

### Prepare images

You can find some X-ray images of lungs here:

* [COVID-19 infected lungs](https://github.com/ieee8023/covid-chestxray-dataset/tree/master/images)
* [Normal lungs](https://www.kaggle.com/paultimothymooney/chest-xray-pneumonia)


### Prepare trained model

You can find a trained model [here](https://djl-tensorflow-javacpp.s3.amazonaws.com/tensorflow-models/covid-19/saved_model.zip),
download and unzip it.

### Run prediction

run the following command:

```
./gradlew run -Dai.djl.default_engine=TensorFlow -Dai.djl.repository.zoo.location=/path/to/saved/model --args="/path/to/image"
```


## Reference:

1. [Detecting COVID-19 in X-ray images with Keras, TensorFlow, and Deep Learning](https://www.pyimagesearch.com/2020/03/16/detecting-covid-19-in-x-ray-images-with-keras-tensorflow-and-deep-learning/) 
2. [DJL TensorFlow Engine](https://github.com/awslabs/djl/tree/master/tensorflow/tensorflow-engine)
3. [DJL TensorFlow Documentation](https://github.com/awslabs/djl/tree/master/docs/tensorflow)