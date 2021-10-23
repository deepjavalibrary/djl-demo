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
Huggingface hybrid model example.
"""

import torch
from djl_python import Input
from djl_python import Output

from transformers_model import BaseNLPModel


class TokenClassification(BaseNLPModel):

    def __init__(self):
        super().__init__("token_classification")
        self.label_list = self.mapping["label_list"].strip('][').split(', ')

    def preprocess(self, data: Input):
        input_text = data.get_as_string()
        tokens = self.tokenizer.encode_plus(input_text,
                                            max_length=self.max_length,
                                            truncation=True,
                                            padding=True,
                                            add_special_tokens=True,
                                            return_tensors='np')
        input_ids = tokens["input_ids"]
        attention_mask = tokens["attention_mask"]
        outputs = Output()
        outputs.add_as_numpy([input_ids, attention_mask])
        return outputs

    def postprocess(self, data: Input):
        input_ids = data.get_as_numpy("input_ids")[0].tolist()
        predictions = torch.from_numpy(data.get_as_numpy("data")[0])
        num_rows = predictions.shape[0]

        outputs = Output()
        result = []
        for i in range(num_rows):
            out = predictions[i].unsqueeze(0)
            out = torch.argmax(out, dim=2)
            tokens = self.tokenizer.tokenize(self.tokenizer.decode(input_ids[i]))
            prediction = [(token, self.label_list[prediction]) for token, prediction in
                          zip(tokens, out[0].tolist())]
            result.append(prediction)

        outputs.add_as_json(result)
        return outputs
