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

import logging
import os

import torch
import torch_neuron
from djl_python import Input
from djl_python import Output
from transformers import AutoTokenizer


class QuestionAnswering(object):

    def __init__(self):
        self.max_length = 128
        self.device = None
        self.model = None
        self.tokenizer = None
        self.initialized = False

    def initialize(self, properties: dict):
        visible_cores = os.getenv("NEURON_RT_VISIBLE_CORES")
        logging.info("NEURON_RT_VISIBLE_CORES: " + visible_cores)

        device_id = properties.get("device_id")
        device_id = "cpu" if device_id == "-1" else "cuda:" + device_id
        self.device = torch.device(device_id)
        self.model = torch.jit.load("question_answering.pt").to(self.device)
        self.tokenizer = AutoTokenizer.from_pretrained(os.getcwd(), do_lower_case=True)
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
                                                padding='max_length',
                                                add_special_tokens=True,
                                                return_tensors="pt")
            input_ids = tokens["input_ids"].to(self.device)
            attention_mask = tokens["attention_mask"].to(self.device)

            inferences = []
            out = self.model(input_ids, attention_mask)
            answer_start_scores = out[0]
            answer_end_scores = out[1]

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

