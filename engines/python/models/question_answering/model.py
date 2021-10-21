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
from transformers import AutoTokenizer, AutoModelForQuestionAnswering


class QuestionAnswering(object):

    def __init__(self):
        self.model_name = None
        self.max_length = None
        self.do_lower_case = None
        self.device = None
        self.model = None
        self.tokenizer = None
        self.initialized = False

    def initialize(self, properties: dict):
        with open("settings.json") as f:
            settings = json.load(f)

        device_id = properties.get("device_id")
        device_id = "cpu" if device_id == "-1" else "cuda:" + device_id
        self.device = torch.device(device_id)
        self.model_name = settings["model_name"]
        self.max_length = int(settings["max_length"])
        self.do_lower_case = settings["do_lower_case"]
        self.model = AutoModelForQuestionAnswering.from_pretrained(self.model_name)
        self.tokenizer = AutoTokenizer.from_pretrained(self.model_name, do_lower_case=self.do_lower_case)
        self.model.to(self.device).eval()
        self.initialized = True

    def inference(self, inputs: Input):
        try:
            data = inputs.get_as_json()
            question = data["question"]
            paragraph = data["paragraph"]
            tokens = self.tokenizer.encode_plus(question,
                                                paragraph,
                                                max_length=self.max_length,
                                                truncation=True,
                                                padding=True,
                                                add_special_tokens=True,
                                                return_tensors="pt")
            input_ids = tokens["input_ids"].to(self.device)
            attention_mask = tokens["attention_mask"].to(self.device)

            inferences = []
            out = self.model(input_ids, attention_mask)
            answer_start_scores = out.start_logits
            answer_end_scores = out.end_logits

            num_rows, num_cols = answer_start_scores.shape
            for i in range(num_rows):
                answer_start_scores_one_seq = answer_start_scores[i].unsqueeze(0)
                answer_start = torch.argmax(answer_start_scores_one_seq)
                answer_end_scores_one_seq = answer_end_scores[i].unsqueeze(0)
                answer_end = torch.argmax(answer_end_scores_one_seq) + 1
                token_id = self.tokenizer.convert_ids_to_tokens(input_ids[i].tolist()[answer_start:answer_end])
                prediction = self.tokenizer.convert_tokens_to_string(token_id)
                inferences.append(prediction)

            outputs = Output()
            outputs.add_as_json(inferences)
        except Exception as e:
            logging.error(e, exc_info=True)
            # error handling
            outputs = Output(code=500, message=str(e))
            outputs.add("inference failed", key="data")

        return outputs


_model = QuestionAnswering()


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
