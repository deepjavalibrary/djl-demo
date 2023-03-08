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
PyTorch resnet18 pre-processing example.
"""

import logging
from typing import Optional

import torch
from torchvision import transforms

from djl_python import Input
from djl_python import Output


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


def handle(inputs: Input) -> Optional[Output]:
    """
    Default handler function
    """
    if inputs.is_empty():
        # initialization request
        return None

    return preprocess(inputs)
