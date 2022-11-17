# DJLServing Python Client Example

This demo project include several examples to demonstrate how to make inference requests against DJL Serving using Python [requests](https://pypi.org/project/requests/) library's POST function.

Note that this demo assumes that DJLServing is already started. Refer [here](https://github.com/deepjavalibrary/djl-serving/blob/master/serving/docs/starting.md) on how to start DJL Serving.

## Example 1

In the first example, let's load an [Image Classification model](https://resources.djl.ai/demo/pytorch/traced_resnet18.zip).

First we need to download the input image.

```
curl -O https://resources.djl.ai/images/kitten.jpg
```

To register the model and make predictions:

```python
import requests

# Register model
params = {'url': 'https://resources.djl.ai/demo/pytorch/traced_resnet18.zip', 'engine': 'PyTorch'}
requests.post('http://localhost:8080/models', params=params)

# Run inference
url = 'http://localhost:8080/predictions/traced_resnet18'
res = requests.post(url, files={'data': open('kitten.jpg', 'rb')})
print(res.text)
```

The above code specifies the image as binary input. Or you can use multipart/form-data format as below:

```python
# Run inference
url = 'http://localhost:8080/predictions/traced_resnet18'
res = requests.post(url, files={'data': open('kitten.jpg', 'rb')})
print(res.text)
```

Run the example:

```
python djlserving_client_example1.py
```

This should return the following result:

```json
[
  {
    "className": "n02123045 tabby, tabby cat",
    "probability": 0.4021684527397156
  },
  {
    "className": "n02123159 tiger cat",
    "probability": 0.2915370762348175
  },
  {
    "className": "n02124075 Egyptian cat",
    "probability": 0.27031460404396057
  },
  {
    "className": "n02123394 Persian cat",
    "probability": 0.007626926526427269
  },
  {
    "className": "n02127052 lynx, catamount",
    "probability": 0.004957367666065693
  }
]
```

## Example 2

In the second example, we load a [HuggingFace Bert QA model](https://mlrepo.djl.ai/model/nlp/question_answer/ai/djl/huggingface/pytorch/deepset/bert-base-cased-squad2/0.0.1/bert-base-cased-squad2.zip) and make predictions.

```python
import requests

# Register model
params = {
    'url': 'https://mlrepo.djl.ai/model/nlp/question_answer/ai/djl/huggingface/pytorch/deepset/bert-base-cased-squad2'
           '/0.0.1/bert-base-cased-squad2.zip',
    'engine': 'PyTorch'
}
requests.post('http://localhost:8080/models', params=params)

# Run inference
url = 'http://localhost:8080/predictions/bert_base_cased_squad2'
data = {"question": "How is the weather", "paragraph": "The weather is nice, it is beautiful day"}
res = requests.post(url, json=data)
print(res.text)
```

The above code passes the data to the server using the Content-Type application/json.

Another way is to use the `json` keyword:

```python
url = 'http://localhost:8080/predictions/bert_base_cased_squad2'
data = {"question": "How is the weather", "paragraph": "The weather is nice, it is beautiful day"}
res = requests.post(url, json=data)
print(res.text)
```

Run the example:

```
python djlserving_client_example2.py
```

This should return the following result:

```
nice
```

## Example 3

In the third example, we can try a [HuggingFace Fill Mask model](https://mlrepo.djl.ai/model/nlp/fill_mask/ai/djl/huggingface/pytorch/bert-base-uncased/0.0.1/bert-base-uncased.zip). Masked model inputs masked words in a sentence and predicts which words should replace those masks.

```python
import requests

# Register model
params = {
    'url': 'https://mlrepo.djl.ai/model/nlp/fill_mask/ai/djl/huggingface/pytorch/bert-base-uncased/0.0.1'
           '/bert-base-uncased.zip',
    'engine': 'PyTorch'
}
requests.post('http://localhost:8080/models', params=params)

# Run inference
url = 'http://localhost:8080/predictions/bert_base_uncased'
data = {"data": "The man worked as a [MASK]."}
res = requests.post(url, json=data)
print(res.text)
```

The above code passes the data to the server using the Content-Type application/json.

Another way is to use the `json` keyword:

```python
url = 'http://localhost:8080/predictions/bert_base_uncased'
data = {"data": "The man worked as a [MASK]."}
res = requests.post(url, json=data)
print(res.text)
```

Run the example:

```
python djlserving_client_example3.py
```

This should return the following result:

```json
[
  {
    "className": "carpenter",
    "probability": 0.05010193586349487
  },
  {
    "className": "salesman",
    "probability": 0.027945348992943764
  },
  {
    "className": "mechanic",
    "probability": 0.02747158892452717
  },
  {
    "className": "cop",
    "probability": 0.02429874986410141
  },
  {
    "className": "contractor",
    "probability": 0.024287723004817963
  }
]
```