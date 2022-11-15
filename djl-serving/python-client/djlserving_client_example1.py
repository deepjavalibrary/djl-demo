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
DJLServing Python client example - Image Classification
"""
import requests

# Register model
url = 'http://localhost:8080/models'
params = {'url': 'https://resources.djl.ai/demo/pytorch/traced_resnet18.zip', 'engine': 'PyTorch'}
res = requests.post(url, params=params)

# Run inference
url = 'http://localhost:8080/predictions/traced_resnet18'
headers = {'Content-Type': 'application/octet-stream'}
with open('/tmp/kitten.jpg', 'rb') as f:
    data = f.read()
res = requests.post(url, data=data, headers=headers)
print(res.text)

# Another way to run inference
url = 'http://localhost:8080/predictions/traced_resnet18'
res = requests.post(url, files={'data': open('/tmp/kitten.jpg', 'rb')})
print(res.text)
