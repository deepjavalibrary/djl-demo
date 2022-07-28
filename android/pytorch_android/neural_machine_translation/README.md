# Neural Machine Translation

In this example, you will see how to do Neural Machine Translation with pre-trained
encoder and decoder models. You
will translate text from French to English using two different models. First, the French input will
go through the encoder model, and then through the decoder model to be converted into English text.

To use the app, type in some French text (e.g. trop tard). Then press the translate button and the translated text
in English will appear in the lower box. If you want to try different French text, simply delete
what you originally typed in, and replace it with your desired text.

![](https://resources.djl.ai/demo/pytorch/android/neural_machine_translation/NMTexample1.png)


### Setup
Use the following command to install this app on your Android phone:

```
cd neural_machine_translation

# for Linux/macOS
./gradlew iD

# for Windows
..\gradlew iD
```

It will install the Neural Machine Translation application on your Android phone.
