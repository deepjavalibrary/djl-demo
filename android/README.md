# DJL Android Demo

# Introduction
This demo project include several fun apps with DJL.

## QuickDraw Doodle recognition
Rabbit                     |  Cat                      | banana
:-------------------------:|:-------------------------:|:-------------------------:
![](img/rabbit.gif)        |  ![](img/cat.gif)         | ![](img/banana.gif) 

[Quickdraw dataset](https://github.com/googlecreativelab/quickdraw-dataset) were created by Google 
that includes millions of hand-drawn doodle. The model were trained through 
the subset from [kaggle challenge](https://www.kaggle.com/c/quickdraw-doodle-recognition).

Model were trained with MobileNet on PyTorch and achieved 91.7% top 3 accuracy. 
This application demos how user can import PyTorch model and run inference on their device.

The minimum API level for this project is 26.
