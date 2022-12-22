# Footwear classification

Image classification refers to the task of extracting information classes from an image. In this demo, you learn how
to train your model to be able to classify images. The source code for this project came from the blog post
[Deep Java Library(DJL) — a Deep Learning Toolkit for Java Developers](https://towardsdatascience.com/deep-java-library-djl-a-deep-learning-toolkit-for-java-developers-55d5a45bca7e)

In this demo we will use DJL to train a multiclass classification model. The dataset that we use is called
footwear classification and can classify images into four classes, namely, boots, sandals, shoes, or slippers.

## Preparing the footwear dataset

This project contains the information from this dataset:
- [UT Zappos50K](http://vision.cs.utexas.edu/projects/finegrained/utzap50k/)

First you must download the dataset from [UT Zappos50K](http://vision.cs.utexas.edu/projects/finegrained/utzap50k/). In
that website you must download the file named "ut-zap50k-images-square.zip". Unzip the file and put unzipped file
into the `ut-zap50k-images-square` folder.

Use the following commands to download and unzip your footwear dataset in Linux:

```shell
cd footwear_classification
curl -O https://vision.cs.utexas.edu/projects/finegrained/utzap50k/ut-zap50k-images-square.zip
unzip ut-zap50k-images-square.zip
```

### Load the dataset from local folder

You can use [ImageFolder](https://javadoc.io/ai/djl/api/dataset/ImageFolder) class to load the dataset.
`ImageFolder` by default use subfolder names as image class names. You should see four subfolders inside
`ut-zap50k-images-square`: Boots, Sandals, Shoes, and Slippers. These four folders will be used as labels.

Since the images files are in 2 levels down in the sub folders, you need to specify `maxDepth` for
the `ImageFolder`:

```java
    ImageFolder dataset = ImageFolder.builder()
        .setRepositoryPath(Paths.get(datasetRoot)) // set root of image folder
        .optMaxDepth(10) // set the max depth of the sub folders
        .addTransform(new Resize(Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT))
        .addTransform(new ToTensor())
        .setSampling(BATCH_SIZE, true) // random sampling; don't process the data in order
        .build();
```


## Train the footwear classification model

By running this command will train this model for 2 epochs.

```shell
cd footwear_classification

# for Linux/macOS
./gradlew training

# for Windows:
..\..\gradlew training
```

The output should look something like this:

```
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Training on: cpu().
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Load MXNet Engine Version 1.9.0 in 0.073 ms.
Training:    100% |████████████████████████████████████████| Accuracy: 0.88, SoftmaxCrossEntropyLoss: 0.37
Validating:  100% |████████████████████████████████████████|
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Epoch 1 finished.
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Train: Accuracy: 0.88, SoftmaxCrossEntropyLoss: 0.37
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Validate: Accuracy: 0.90, SoftmaxCrossEntropyLoss: 0.31
Training:    100% |████████████████████████████████████████| Accuracy: 0.92, SoftmaxCrossEntropyLoss: 0.22
Validating:  100% |████████████████████████████████████████|
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Epoch 2 finished.
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Train: Accuracy: 0.92, SoftmaxCrossEntropyLoss: 0.22
[main] INFO ai.djl.training.listener.LoggingTrainingListener - Validate: Accuracy: 0.93, SoftmaxCrossEntropyLoss: 0.21
[main] INFO ai.djl.training.listener.LoggingTrainingListener - forward P50: 92.115 ms, P90: 98.226 ms
[main] INFO ai.djl.training.listener.LoggingTrainingListener - training-metrics P50: 0.035 ms, P90: 0.060 ms
[main] INFO ai.djl.training.listener.LoggingTrainingListener - backward P50: 9.760 ms, P90: 12.717 ms
[main] INFO ai.djl.training.listener.LoggingTrainingListener - step P50: 20.774 ms, P90: 24.168 ms
[main] INFO ai.djl.training.listener.LoggingTrainingListener - epoch P50: 744.727 s, P90: 744.727 s
```
The model will be stored in the  `models` folder.

## Run footwear classification

Once you trained your model, you can use following command to run footwear classification:

```shell
# for Linux/macOS:
./gradlew inference

# for Windows:
..\gradlew infrerence

[
        class: "Sandals", probability: 0.68156
        class: "Shoes", probability: 0.31769
        class: "Slippers", probability: 0.00060
        class: "Boots", probability: 0.00013
]
```

You can pass different image files to be classified:
```shell
# for Linux/macOS:
./gradlew inference --args="ut-zap50k-images-square/Boots/Ankle/Reef/8031227.20.jpg"

# for Windows:
..\gradlew inference --args="ut-zap50k-images-square/Boots/Ankle/Reef/8031227.20.jpg"

[
        class: "Boots", probability: 0.78546
        class: "Shoes", probability: 0.17563
        class: "Slippers", probability: 0.03733
        class: "Sandals", probability: 0.00156
]
```
