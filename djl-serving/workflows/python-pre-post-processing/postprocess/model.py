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
PyTorch resnet18 post-processing example.
"""

import json
import logging
import os
from typing import Optional, Any

import torch
import torch.nn.functional as F

from djl_python import Input
from djl_python import Output


class Processing(object):

    def __init__(self):
        self.topK = 5
        self.mapping = None
        self.initialized = False

    def initialize(self, properties: dict):
        """
        Initialize model.
        """
        self.mapping = self.load_label_mapping("index_to_name.json")
        self.initialized = True


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

    @staticmethod
    def load_label_mapping(mapping_file_path: Any) -> dict:
        if not os.path.isfile(mapping_file_path):
            raise Exception('mapping file not found: ' + mapping_file_path)

        with open(mapping_file_path) as f:
            mapping = json.load(f)
        if not isinstance(mapping, dict):
            raise Exception('mapping file should be in "class":"label" format')

        for key, value in mapping.items():
            new_value = value
            if isinstance(new_value, list):
                new_value = value[-1]
            if not isinstance(new_value, str):
                raise Exception(
                    'labels in mapping must be either str or [str]')
            mapping[key] = new_value
        return mapping


_service = Processing()


def handle(inputs: Input) -> Optional[Output]:
    """
    Default handler function
    """
    if inputs.is_empty():
        return None

    if not _service.initialized:
        # stateful model
        _service.initialize(inputs.get_properties())

    return _service.postprocess(inputs)
