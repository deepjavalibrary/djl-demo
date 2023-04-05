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
PyTorch resnet18 model example.
"""

import itertools
import json
import logging
import os

import torch
import torch.nn.functional as F
from torchvision import models
from torchvision import transforms

from djl_python import Input
from djl_python import Output


class Resnet18(object):
    """
    Resnet18 Model implementation.
    """
    def __init__(self):
        self.device = None
        self.model = None
        self.image_processing = None
        self.topK = 5
        self.mapping = None
        self.initialized = False

    def initialize(self, properties: dict):
        """
        Initialize model.
        """
        device_id = properties.get("device_id")
        device_id = "cpu" if device_id == "-1" else "cuda:" + device_id
        self.device = torch.device(device_id)
        self.model = models.resnet18(pretrained=True).to(self.device)
        self.model.eval()
        self.image_processing = transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406],
                                 std=[0.229, 0.224, 0.225])
        ])
        self.mapping = self.load_label_mapping("index_to_name.json")
        self.initialized = True

    def inference(self, inputs):
        """
        Custom service entry point function.

        :param inputs: the Input object holds a list of numpy array
        :return: the Output object to be send back
        """
        outputs = Output()
        try:
            content_type = inputs.get_property("Content-Type")
            if content_type is not None and content_type.startswith("tensor/"):
                images = torch.from_numpy(inputs.get_as_numpy()[0]).to(self.device)
                data = self.model(images).to(torch.device('cpu'))
                outputs.add_property("Content-Type", "tensor/ndlist")
                outputs.add_as_numpy([data.detach().numpy()])
                return outputs

            image = inputs.get_as_image()
            image = self.image_processing(image)
            images = torch.stack([image]).to(self.device)

            data = self.model(images)
            ps = F.softmax(data, dim=1)
            probs, classes = torch.topk(ps, self.topK, dim=1)
            probs = probs.tolist()
            classes = classes.tolist()

            outputs.add_as_json(
                self.map_class_to_label(probs, self.mapping, classes))
        except Exception as e:
            logging.error(e, exc_info=True)
            # error handling
            outputs.set_code(500)
            outputs.set_message(str(e))
            outputs.add("inference failed", key="data")

        return outputs

    @staticmethod
    def load_label_mapping(mapping_file_path):
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

    @staticmethod
    def map_class_to_label(probs, mapping=None, lbl_classes=None):
        if not (isinstance(probs, list) and isinstance(probs, list)):
            raise Exception('Convert classes to list before doing mapping')
        if mapping is not None and not isinstance(mapping, dict):
            raise Exception('Mapping must be a dict')

        if lbl_classes is None:
            lbl_classes = itertools.repeat(range(len(probs[0])), len(probs))

        results = [{(mapping[str(lbl_class)]
                     if mapping is not None else str(lbl_class)): prob
                    for lbl_class, prob in zip(*row)}
                   for row in zip(lbl_classes, probs)]

        return results


_service = Resnet18()


def handle(inputs: Input):
    """
    Default handler function
    """
    if not _service.initialized:
        # stateful model
        _service.initialize(inputs.get_properties())

    if inputs.is_empty():
        # initialization request
        return None

    return _service.inference(inputs)
