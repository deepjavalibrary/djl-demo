# DJLServing: Java mode

There two options to deploy java model on DJLServing:

- Package only model artifacts with metadata data (`serving.properties`), and install jar
  manually in DJLServing. This option allows you to update model artifacts and it's inference code
  independently.
- Package your java source code (or jar) with the model.

## Prepare your Java file

You can use the `devEnv` folder to start building your own translator. The translator is a Java
interface defined in DJL for pre/post processing.
Once it is done, you can package the whole project as an uber jar by doing

```bash
cd djl-serving/java-mode/devEnv
./gradlew jar
```

Then copy the jar in the `build/libs` folder to the inference folder under `libs`.

### Add extra Jars to your inference

If you have extra Java libraries needs to import alone: In the inference folder, add a folder
namely `libs`, then place all of your jars in here.

### Add extra Java file to your inference

You can still do this by adding your .java file in the `libs/classes` folder

## Run example

There two options to package your java classes:

### Step 1: Download TorchScript model file

```bash
mkdir resnet18
curl https://mlrepo.djl.ai/model/cv/image_classification/ai/djl/pytorch/synset.txt -o resnet18/synset.txt
curl https://resources.djl.ai/test-models/traced_resnet18.pt -o resnet18/resnet18.pt
```

### Step 2: Copy your Translator to the model folder (optional)

You can manually copy the jar file into DJLServing's `deps` folder.

```bash
mkdir -p resnet18/libs
cp build/libs/*.jar resnet18/libs/

# or just copy classes if there is dependencies
mkdir -p resnet18/libs/classes
cp -r src/main/java/ resnet18/libs/classes
```

### Step 3: Create serving.properties (optional)

```bash
echo "engine=PyTorch" > serving.properties
echo "option.modelName=resnet18" >> serving.properties
echo "translatorFactory=com.examples.CustomTranslator" >> serving.properties 
```

### Step 4: Start your inference

Under the current folder, just run with the endpoint, assume my endpoint name is `resnet`. The model
we used is a PyTorch resnet18 model, then we add the name PyTorch in the instructions.

Linux/macOS

```bash
djl-serving -m resnet::PyTorch=file://$PWD/resnet18
```

Windows

```
path-to-your\serving.bat -m "resnet::PyTorch=file:///%cd%\resnet18"
```

### Step 4: Run inference

Open another session to run inference

Linux/macOS

```bash
curl -O https://resources.djl.ai/images/kitten.jpg
curl -X POST "http://127.0.0.1:8080/predictions/resnet" -T "kitten.jpg"
```

On Windows, you can just download the image and use tool similar to curl to send POST request.
