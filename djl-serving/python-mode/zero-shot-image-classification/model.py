#!/usr/bin/env python
#
# Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
# except in compliance with the License. A copy of the License is located at
#
# http://aws.amazon.com/apache2.0/
#
# or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for
# the specific language governing permissions and limitations under the License.

import logging

import requests
import torch
from PIL import Image
from transformers import AutoProcessor, AutoModelForZeroShotObjectDetection

from djl_python import Input
from djl_python import Output


class ZeroShotObjectDetection(object):

    def __init__(self):
        self.device = None
        self.model = None
        self.processor = None
        self.initialized = False

    def initialize(self, properties: dict):
        """
        Initialize model.
        """
        model_id = "IDEA-Research/grounding-dino-base"
        device_id = properties.get("device_id", "-1")
        device_id = "cpu" if device_id == "-1" else "cuda:" + device_id
        self.device = torch.device(device_id)
        self.processor = AutoProcessor.from_pretrained(model_id)
        self.model = AutoModelForZeroShotObjectDetection.from_pretrained(
            model_id).to(self.device)
        self.initialized = True

    def inference(self, inputs):
        outputs = Output()
        try:
            batch = inputs.get_batches()
            images = []
            text = []
            sizes = []
            for i, item in enumerate(batch):
                data = item.get_as_json()
                data = data.pop("inputs", data)
                image = Image.open(
                    requests.get(data["image_url"]["url"], stream=True).raw)
                images.append(image)
                text.append(data["text"])
                sizes.append(image.size[::-1])

            model_inputs = self.processor(images=images,
                                          text=text,
                                          return_tensors="pt").to(self.device)
            with torch.no_grad():
                model_outputs = self.model(**model_inputs)

            results = self.processor.post_process_grounded_object_detection(
                model_outputs,
                model_inputs.input_ids,
                box_threshold=0.4,
                text_threshold=0.3,
                target_sizes=sizes)
            for i, result in enumerate(results):
                ret = {
                    "labels": result["labels"],
                    "scores": result["scores"].tolist(),
                    "boxes": result["boxes"].cpu().detach().numpy().tolist(),
                }
                if inputs.is_batch():
                    outputs.add_as_json(ret, batch_index=i)
                else:
                    outputs.add_as_json(ret)
        except Exception as e:
            logging.exception("ZeroShotObjectDetection inference failed")
            # error handling
            outputs = Output().error(str(e))

        return outputs


_service = ZeroShotObjectDetection()


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
