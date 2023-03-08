# How to run python pre/post processing

This is an example of how to run python pre/post processing in DJL application.

Many developers facing a common problem that it's difficult to port python processing code into Java.
For example some video processing library may not have equivalent in java. Or java library
may output slightly different result than python, which may impact inference accuracy.

You can run the model code with DJL's Python engine today, however, you won't get multi-threading
benefit that DJL provides. To quickly port a python model in DJL and gain multi-threading advantage,
you can load your model in DJL, and keep pre/post processing code in Python. 

## A simple solution

In this simple solution, you will learn how to use Python engine to invoke a python function from
DJL and pass input/output back and force.

### Create a python model

First you need create a [`model.py`](src/test/resources/resnet18/model.py) file. In this file, you
need to define three functions:

- `def handle(inputs: Input) -> Optional[Output]:`, this required for DJL Python engine to load the model
- `def preprocess(inputs: Input) -> Output:`, a function for pre-process in this demo
- `def postprocess(inputs: Input) -> Output:`, a function for post-process in this demo

```python
def preprocess(inputs: Input) -> Output:
    pass


def postprocess(inputs: Input) -> Output:
    pass


def handle(inputs: Input) -> Optional[Output]:
    return None
```

### Load python model and invoke `preprocess()` and `postprocess()`

You can use regular `Criteria` to load the python model, in order to pass arbitrary data to the
python function, we use `Input` and `Output` class in this demo. Once the model is loaded,
you case set `handler` properties in each request to specify which function you want to call:
`preProcessing.addProperty("handler", "preprocess")`

```java
Input preProcessing = new Input();
preProcessing.add("data", image);
preProcessing.addProperty("Content-Type", "image/jpeg");
// calling preprocess() function in model.py
preProcessing.addProperty("handler", "preprocess");
Output preprocessed = processingPredictor.predict(preProcessing);
```

## A better solution

You have learned how to invoke an arbitrary python function and chain **pre-process**,
**forward** and **post-process** together. Now ou can organize your code in better way following
DJL's `Translator` design pattern:

### Create a `PythonTranslator`

`Translator` is a DJL design pattern that allows you define your own pre-process and post-process.

You can load python model in the translator and invoke python `preprocess()` and `postprocess()`
function in the translator. See: [PythonTranslator](src/main/java/com/examples/PythonTranslator.java) code.

### Put them together

Once you created `PythonTranslator`, everything becomes much easy. The following code just look
like a regular DJL 

```java
PythonTranslator translator = new PythonTranslator();
Criteria<String, Classifications> criteria =
        Criteria.builder()
                .setTypes(String.class, Classifications.class)
                .optModelUrls("djl://ai.djl.pytorch/resnet")
                .optTranslator(translator)
                .build();
String url = "https://resources.djl.ai/images/kitten.jpg";
try (ZooModel<String, Classifications> model = criteria.loadModel();
        Predictor<String, Classifications> predictor = model.newPredictor()) {
    Classifications ret = predictor.predict(url);
    System.out.println(ret);
}

// unload python model
translator.close();
```

## Use djl-serving workflow

Using Python engine directly in Java has some limitation, the predictor can only be executed in
a single thread. If you use python predictor in multiple threads, the request is executed one by
one and can not be parallelized. You have to manage a pool of predictor in multi-threading case.

If you use DJLServing, deploy python model and PyTorch model in the model server, DJLServing will
automatically scale both models based in traffic.

You can use DJLServing's workflow feature, group multiple models as a single workflow.

See: https://github.com/deepjavalibrary/djl-demo/tree/master/djl-serving/workflows/

## Run/test application with gradle

```
cd development/python

./gradlew run

[main] INFO ai.djl.pytorch.engine.PtEngine - Number of inter-op threads is 6
[main] INFO ai.djl.pytorch.engine.PtEngine - Number of intra-op threads is 6
[main] INFO ai.djl.python.engine.PyEnv - Found requirements.txt, start installing Python dependencies...
[main] INFO ai.djl.python.engine.PyEnv - pip install requirements succeed!

{
	"tabby": 0.43127959966659546,
	"tiger_cat": 0.3664361238479614,
	"Egyptian_cat": 0.17378537356853485,
	"tiger": 0.0060998848639428616,
	"plastic_bag": 0.003765479428693652
}
```
