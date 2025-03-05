#!/usr/bin/env python
#
# Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
# except in compliance with the License. A copy of the License is located at
#
# http://aws.amazon.com/apache2.0/
#
# or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for
# the specific language governing permissions and limitations under the License.

"""
DJLServing Python client example - Fill Mask
"""
import requests
import json

# Register model
params = {
    'url': 'https://mlrepo.djl.ai/model/nlp/fill_mask/ai/djl/huggingface/pytorch/bert-base-uncased/0.0.1'
           '/bert-base-uncased.zip',
    'engine': 'PyTorch'
}
requests.post('http://localhost:8080/models', params=params)

# Run inference
url = 'http://localhost:8080/predictions/bert_base_uncased'
data = {"inputs": "The man worked as a [MASK]."}
res = requests.post(url, json=data)
print(res.text)

# Another way to run inference with explicit content-type
headers = {'Content-Type': 'application/json'}
res = requests.post(url, data=json.dumps(data), headers=headers)
print(res.text)
