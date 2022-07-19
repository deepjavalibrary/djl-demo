# Semantic Segmentation

In this example, you will see how to do semantic segmentation with a pre-trained 
[DeepLabV3](https://pytorch.org/hub/pytorch_vision_deeplabv3_resnet101/) model. You
will identify objects in images and color them in based on the type of object it is.

To use the app, press the Segment button for the image. It will replace the unedited image with the
semantic image that has the identified objects colored in. To move on to the next picture, click
the restart button.

|                                       Before                                        |                                       After                                        |
|:-----------------------------------------------------------------------------------:|:----------------------------------------------------------------------------------:|
| ![](https://resources.djl.ai/demo/pytorch/android/semantic_segmentation/before.png) | ![](https://resources.djl.ai/demo/pytorch/android/semantic_segmentation/after.png) |

It has a ResNet-50 backbone with a 92% accuracy. It was trained with a subset of the COCO train2017 dataset. This demo 
shows how the model can perform inference on a user's device.

### Setup
Use the following command to install this app on your Android phone:

```
cd semantic_segmentation

# for Linux/macOS
./gradlew iD

# for Windows
..\gradlew iD
```

It will install the Semantic Segmentation application on your Android phone.
