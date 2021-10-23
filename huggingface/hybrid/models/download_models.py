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
import os

import torch

import transformers
from transformers import (
    AutoModelForSequenceClassification,
    AutoTokenizer,
    AutoModelForQuestionAnswering,
    AutoModelForTokenClassification,
    AutoConfig)


def transformers_model_downloader(app):
    model_file = os.path.join("hybrid", app + ".pt")
    if os.path.isfile(model_file):
        print("model already downloaded: " + model_file)
        return

    settings_file = os.path.join("hybrid", app + "_config.json")
    if not os.path.isfile(settings_file):
        print("Unknown application: " + app)
        return

    print("Download model for: ", model_file)

    with open(settings_file) as f:
        settings = json.load(f)

    model_name = settings["model_name"]
    num_labels = int(settings["num_labels"])
    max_length = int(settings["max_length"])
    do_lower_case = settings["do_lower_case"]
    if app == "text_classification":
        config = AutoConfig.from_pretrained(model_name, num_labels=num_labels, torchscript=True)
        model = AutoModelForSequenceClassification.from_pretrained(model_name, config=config)
        tokenizer = AutoTokenizer.from_pretrained(model_name, do_lower_case=do_lower_case)
    elif app == "question_answering":
        config = AutoConfig.from_pretrained(model_name, torchscript=True)
        model = AutoModelForQuestionAnswering.from_pretrained(model_name, config=config)
        tokenizer = AutoTokenizer.from_pretrained(model_name, do_lower_case=do_lower_case)
    elif app == "token_classification":
        config = AutoConfig.from_pretrained(model_name, num_labels=num_labels, torchscript=True)
        model = AutoModelForTokenClassification.from_pretrained(model_name, config=config)
        tokenizer = AutoTokenizer.from_pretrained(model_name, do_lower_case=do_lower_case)
    else:
        print("Unknown application: " + app)
        return

    dummy_input = "This is a dummy input for torch jit trace"
    inputs = tokenizer.encode_plus(dummy_input, max_length=max_length, pad_to_max_length=True,
                                   add_special_tokens=True, return_tensors='pt')
    input_ids = inputs["input_ids"]
    attention_mask = inputs["attention_mask"]
    model.eval()
    traced_model = torch.jit.trace(model, (input_ids, attention_mask))
    torch.jit.save(traced_model, os.path.join("hybrid", app + ".pt"))


if __name__ == "__main__":
    print('Transformers version: ', transformers.__version__)

    transformers_model_downloader("text_classification")
    transformers_model_downloader("token_classification")
    transformers_model_downloader("question_answering")
