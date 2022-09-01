# Speech Recognition

In this example, you will see how to do speech recognition with a pre-trained
[wav2vec2](https://github.com/facebookresearch/fairseq/tree/main/examples/wav2vec) model. You
will identify sounds and convert them to text.

To use the app, press the Start button. It will listen to your audio for six seconds. When these 
six seconds are up, it will attempt to transcribe what you said into text. This app works best on
a real Android phone.

![](https://djl-misc.s3.amazonaws.com/tmp/speech_recognition/ai/djl/pytorch/wav2vec2/speech.jpg) 

This model was trained on the [LibriSpeech](http://www.openslr.org/12) dataset. This demo
shows how the model can perform inference on a user's device.

### Setup
Use the following command to install this app on your Android phone:

```
cd speech_recognition

# for Linux/macOS
./gradlew iD

# for Windows
..\gradlew iD
```

It will install the Speech Recognition application on your Android phone.