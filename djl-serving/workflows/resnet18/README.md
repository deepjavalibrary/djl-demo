# DJLServing Workflow Example

This is an example of a DJLServing Workflow. 

## Setup

Before starting, you need to install DJLServing. You can find installation instructions
[here](https://github.com/deepjavalibrary/djl-serving#installation).

## Define the workflow

First, in preprocess/model.py we define the `preprocess` function that apply transformations such as resize.

```python
def preprocess(inputs: Input) -> Output:
    image_processing = transforms.Compose([
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406],
                             std=[0.229, 0.224, 0.225])
    ])
    outputs = Output()
    try:
        batch = inputs.get_batches()
        images = []
        for i, item in enumerate(batch):
            image = image_processing(item.get_as_image())
            images.append(image)
        images = torch.stack(images)
        outputs.add_as_numpy(images.detach().numpy())
        outputs.add_property("content-type", "tensor/ndlist")
    except Exception as e:
        logging.exception("pre-process failed")
        # error handling
        outputs = Output().error(str(e))

    return outputs
```

Next, in postprocess/model.py we define the `postprocess` function that apply softmax function to convert
logits to probabilities and map to class names.

```python
def postprocess(self, inputs: Input) -> Output:
    outputs = Output()
    try:
        data = inputs.get_as_numpy(0)[0]
        for i in range(len(data)):
            item = torch.from_numpy(data[i])
            ps = F.softmax(item, dim=0)
            probs, classes = torch.topk(ps, self.topK)
            probs = probs.tolist()
            classes = classes.tolist()
            result = {
                self.mapping[str(classes[i])]: probs[i]
                for i in range(self.topK)
            }
            outputs.add_as_json(result, batch_index=i)
    except Exception as e:
        logging.exception("post-process failed")
        # error handling
        outputs = Output().error(str(e))

    return outputs
```

Finally, we define the resnet18.json to define the workflow.

```
{
  "name": "resnet18",
  "version": "0.1",
  "models": {
    "preprocess": "preprocess/",
    "model": "djl://ai.djl.pytorch/resnet?translatorFactory=ai.djl.translate.NoopServingTranslatorFactory",
    "postprocess": "postprocess/"
  },
  "workflow": {
    "preprocessed": ["preprocess", "in"],
    "inferenced": ["model", "preprocessed"],
    "out": ["postprocess", "inferenced"]
  }
}
```

This defines a workflow named `resnet18`. It first applies preprocessing to the input "in".
Then applies "model" to the result of preprocessing stored in the value "preprocessed".
Finally applies postprocessing to the inferenced result output to the output "out".

## Run the example

### Step 1: Start the model server

Under the current folder, start DJLServing and load the workflow at startup:

```
djl-serving -w file://$PWD/resnet18.json
```

If DJLServing is already started, use the management API to register the workflow:

```
curl -X POST "http://localhost:8080/workflows?url=file://$PWD/resnet18.json"
```

This will register a workflow called `resnet18`.

### Step 2: Run inference

Open another session to run inference:

```
curl -O https://resources.djl.ai/images/kitten.jpg
curl -X POST http://localhost:8080/predictions/resnet18 -T kitten.jpg
```

This is the expected output:

```
[
  {
    "tabby":0.4266396760940552,
    "tiger_cat":0.37516337633132935,
    "Egyptian_cat":0.16760064661502838,
    "tiger":0.005144408904016018,
    "plastic_bag":0.00433959998190403
  }
]
```
