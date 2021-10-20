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

import json
import logging

import torch
from djl_python import Input
from djl_python import Output
from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification,
    AutoConfig)


class TextClassification(object):

    def __init__(self):
        self.model_name = None
        self.num_labels = None
        self.max_length = None
        self.do_lower_case = None
        self.device = None
        self.model = None
        self.tokenizer = None
        self.mapping = None
        self.initialized = False

    def initialize(self, properties: dict):
        with open("config.json") as f:
            settings = json.load(f)

        with open("labels.json") as f:
            self.mapping = json.load(f)

        device_id = properties.get("device_id")
        device_id = "cpu" if device_id == "-1" else "cuda:" + device_id
        self.device = torch.device(device_id)
        self.model_name = settings["model_name"]
        self.num_labels = int(settings["num_labels"])
        self.max_length = int(settings["max_length"])
        self.do_lower_case = settings["do_lower_case"]
        config = AutoConfig.from_pretrained(self.model_name, num_labels=self.num_labels, torchscript=False)
        self.model = AutoModelForSequenceClassification.from_pretrained(self.model_name, config=config)
        self.tokenizer = AutoTokenizer.from_pretrained(self.model_name, do_lower_case=self.do_lower_case)
        self.model.to(self.device).eval()
        self.initialized = True

    def inference(self, inputs: Input):
        try:
            text = inputs.get_as_string()
            tokens = self.tokenizer.encode_plus(text,
                                                max_length=self.max_length,
                                                truncation=True,
                                                padding=True,
                                                add_special_tokens=True,
                                                return_tensors="pt")
            input_ids = tokens["input_ids"].to(self.device)
            attention_mask = tokens["attention_mask"].to(self.device)

            inferences = []
            out = self.model(input_ids, attention_mask)

            num_rows, num_cols = out[0].shape
            for i in range(num_rows):
                prediction = out[0][i].unsqueeze(0)
                y_hat = prediction.argmax(1).item()
                predicted_idx = str(y_hat)
                inferences.append(self.mapping[predicted_idx])

            outputs = Output()
            outputs.add_as_json(inferences)
        except Exception as e:
            logging.error(e, exc_info=True)
            # error handling
            outputs = Output(code=500, message=str(e))
            outputs.add("inference failed", key="data")

        return outputs


_model = TextClassification()


def handle(inputs: Input):
    """
    Default handler function
    """
    if not _model.initialized:
        _model.initialize(inputs.get_properties())

    if inputs.is_empty():
        # initialization request
        return None

    return _model.inference(inputs)
