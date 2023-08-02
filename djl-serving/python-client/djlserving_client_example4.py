#!/usr/bin/env python
#
# Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
DJLServing Python client example - Image Classification binary mode
"""
import requests
import io
import numpy as np

# Register model in tensor in/tensor out mode
params = {'url': 'djl://ai.djl.pytorch/resnet?translatorFactory=ai.djl.translate.NoopServingTranslatorFactory', 'engine': 'PyTorch'}
requests.post('http://localhost:8080/models', params=params)

# Run inference
url = "http://localhost:8080/predictions/resnet"
headers = {"Content-Type": "tensor/npz", "Accept": "tensor/npz"}

# prepare npz inputs
data = np.zeros((1, 3, 224, 224), dtype=np.float32)
memory_file = io.BytesIO()
np.savez(memory_file, data)
memory_file.seek(0)

response = requests.post(url, data=memory_file.read(), headers=headers)

print(f"Response content-Type: {response.headers['content-type']}")
list = np.load(io.BytesIO(response.content))
output = list["arr_0"]
print(f"Output shape: {output.shape}")

