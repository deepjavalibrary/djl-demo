# Deploy Huggingface model with DJL

This is an example of how to deploy Huggingface transformer models in Java without converting
their pre/post processing code into java.

Serving a deep learning models in python has several known limitations. Due to python's GIL,
multiprocessing is commonly used in python model serving solutions. The same model was loaded
into each process and consume large amount of memory. Running deep learning model in multi-threading
model with Java or C++ has much better performance. However, converting existing python based
pre-processing/post-processing code into Java or C++ requires a lot of efforts and is very costly.

DJL provides a [python engine](https://github.com/deepjavalibrary/djl-serving/tree/master/engines/python)
and allows developer to take advantage of DJL's multi-threading and use existing python code
to deploy model in production quickly.

## Setup
Go into the `model` directory and run the following command to download Huggingface models:

```
cd models

pip install requirements.txt
python download_models.py
```

## Build and run the example

Run the following command to run the project:

```shell
cd engines/python

# for Linux/macOS:
./gradlew run

# for Windows:
..\gradlew run
```

## Migrate your python code

In order to deploy your model in DJL, you need download torchscript model,
the [`download_models.py`](https://github.com/deepjavalibrary/djl-demo/blob/master/huggingface/hybrid/models/download_models.py) will convert Huggingface models and save
torchscript models in `models/hybrid` folder. You can change configuration files
in `models/hybrid` folder to other Huggingface models you want to try.

[`model.py`](https://github.com/deepjavalibrary/djl-demo/blob/master/huggingface/hybrid/models/hybrid/model.py) is the entry point. It contains pre-processing and
post-processing functions for *text_classification*, *question_answering* and *token_classification*
use cases:

- text_classification_preprocess(inputs: Input)
- text_classification_postprocess(inputs: Input)
- token_classification_preprocess(inputs: Input)
- token_classification_postprocess(inputs: Input)
- question_answering_preprocess(inputs: Input)
- question_answering_postprocess(inputs: Input)

## Test your python model

DJL provides a tool to allow you test your python model locally in python. Install `djl_python`
follow the [instruction](https://github.com/deepjavalibrary/djl-serving/tree/master/engines/python#test-your-python-model)

```shell
# test text_classification model's pre-processing
python -m djl_python.test_model --model-dir models/hybrid --handler text_classification_preprocess --input models/example_inputs/text_classification.txt

# test text_classification model's post-processing
python -m djl_python.test_model --model-dir models/hybrid --handler text_classification_postprocess --input build/text_classification_output.ndlist

# test question_answering model's pre-processing
python -m djl_python.test_model --model-dir models/hybrid --handler question_answering_preprocess --input models/example_inputs/question_answering.json

# test question_answering model's post-processing
python -m djl_python.test_model --model-dir models/hybrid --handler question_answering_postprocess --input data=build/question_answering_output.ndlist input_ids=build/question_answering_ids.ndlist

# test token_classification model's pre-processing
python -m djl_python.test_model --model-dir models/hybrid --handler token_classification_preprocess --input models/example_inputs/token_classification.txt

# test token_classification model's post-processing
python -m djl_python.test_model --model-dir models/hybrid --handler token_classification_postprocess --input data=build/token_classification_output.ndlist input_ids=build/token_classification_ids.ndlist
```
