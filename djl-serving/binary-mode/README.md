# DJLServing: Binary mode

Binary mode just required the model file placed in a folder.

## Run our example

### Step 1: Download the model

```
mkdir resnet18 && cd resnet18
curl https://resources.djl.ai/test-models/traced_resnet18.pt -o resnet18.pt
```

### Step 2: Start model server

Under the current folder, just run with the endpoint, assume my endpoint name is `resnet`. The model we used is a PyTorch resnet18 model, then we add the name PyTorch in the instructions.

```
djl-serving -m resnet::PyTorch=file://$PWD/resnet18
```


### Step 3: Run inference

DJLServing in binary mode currently accepting NDList/Numpy (.npz) encoded input data. The returned data is always falls into NDList encoding.

You can use DJL API to create `NDList` and serialize the `NDList` to bytes as the input.

#### Direct inference

download a sample ndlist encoded data

curl -O https://resources.djl.ai/benchmark/inputs/ones_1_3_224_224.ndlist
curl -X POST "http://127.0.0.1:8080/predictions/resnet" \
    -T "ones_1_3_224_224.ndlist" \
    -H "Content-type: tensor/ndlist"


#### Python client inference

You can also run the `inference.py` to see how it interact with the server in python:

```
python inference.py
```

User are required to build their own client to do encoding/decoding.