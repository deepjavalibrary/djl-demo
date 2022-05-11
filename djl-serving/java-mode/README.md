# DJLServing: Java mode

## Prepare your Java file

You can use the `devEnv` folder to start building your own translator. The translator is a Java interface defined in DJL for pre/post processing.
Once it is done, you can package the whole project as an uber jar by doing

```
./gradlew jar
```

Then copy the jar in the `build/libs` folder to the inference folder under `libs`.

### Add extra Jars to your inference
If you have extra Java libraries needs to import alone: In the inference folder, add a folder namely `libs`, then place all of your jars in here.

### Add extra Java file to your inference
You can still do this by adding your .java file in the `libs/classes` folder

## Run our example

### Step 1: Download torchscript model file

```
mkdir resnet18 && cd resnet18
curl -O https://mlrepo.djl.ai/model/cv/image_classification/ai/djl/pytorch/synset.txt
curl https://resources.djl.ai/test-models/traced_resnet18.pt -o resnet18.pt
```

### Step 2: Copy your Translator to the classes folder

```
mkdir -p resnet18/libs/classes
cp devEnv/src/main/java/CustomTranslator.java resnet18/libs/classes
```

### Step 3: Start your inference

Under the current folder, just run with the endpoint, assume my endpoint name is `resnet`. The model we used is a PyTorch resnet18 model, then we add the name PyTorch in the instructions.

Linux/macOS

```
djl-serving -m resnet::PyTorch=file://$PWD/resnet18
```

Windows

```
path-to-your\serving.bat -m "resnet::PyTorch=file:///%cd%\resnet18"
```

### Step 4: Run inference

Open another sesssion to run inference

Linux/macOS

```
curl -O https://resources.djl.ai/images/kitten.jpg
curl -X POST "http://127.0.0.1:8080/predictions/resnet" -T "kitten.jpg"
```

On Windows, you can just download the image and use tool similar to curl to send POST request.
