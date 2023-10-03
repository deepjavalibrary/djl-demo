# Inference Recommendation for Large Model Inference solution

This package includes instance, configuration recommendation for large models using LMI containers.

## Installation

To install this package, you can simply do

```bash
pip install git+https://github.com/deepjavalibrary/djl-demo.git#subdirectory=aws/sagemaker/llm-instance-recommend
```

## Usage

You can provide a huggingface model id and get corresponding recommendations:

```python
from lmi_recommender.instance_recommender import instance_recommendation
from lmi_recommender.ir_strategy_maker import decision_maker, decision_pretty_print
from lmi_recommender.lmi_config_recommender import lmi_config_recommender

fp16_recommendation = instance_recommendation("OpenAssistant/llama2-13b-orca-8k-3319")
lmi_config_recommender(fp16_recommendation)
gpu_recommendation, neuron_recommendation = decision_maker(fp16_recommendation)
decision_pretty_print(gpu_recommendation)
```
