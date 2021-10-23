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

import torch
from djl_python import Input
from djl_python import Output

from transformers_model import BaseNLPModel


class QuestionAnswering(BaseNLPModel):

    def __init__(self):
        super().__init__("question_answering")

    def preprocess(self, data: Input):
        input_json = data.get_as_json()
        question = input_json["question"]
        context = input_json["paragraph"]
        tokens = self.tokenizer.encode_plus(question,
                                            context,
                                            max_length=self.max_length,
                                            truncation=True,
                                            padding=True,
                                            add_special_tokens=True,
                                            return_tensors="np")
        input_ids = tokens["input_ids"]
        attention_mask = tokens["attention_mask"]
        outputs = Output()
        outputs.add_as_numpy([input_ids, attention_mask])
        return outputs

    def postprocess(self, data: Input):
        input_ids = data.get_as_numpy("input_ids")[0].tolist()
        predictions = data.get_as_numpy("data")
        answer_start_scores = torch.from_numpy(predictions[0])
        answer_end_scores = torch.from_numpy(predictions[1])
        num_rows, num_cols = answer_start_scores.shape

        outputs = Output()
        result = []
        for i in range(num_rows):
            answer_start_scores_one_seq = answer_start_scores[i].unsqueeze(0)
            answer_start = torch.argmax(answer_start_scores_one_seq)
            answer_end_scores_one_seq = answer_end_scores[i].unsqueeze(0)
            answer_end = torch.argmax(answer_end_scores_one_seq) + 1
            prediction = self.tokenizer.convert_tokens_to_string(
                self.tokenizer.convert_ids_to_tokens(input_ids[i][answer_start:answer_end]))
            result.append(prediction)
        outputs.add_as_json(result)
        return outputs
