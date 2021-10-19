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

from djl_python import Input

from question_answering import QuestionAnswering
from text_classification import TextClassification
from token_classification import TokenClassification


class StatelessModel(object):
    def __init__(self):
        self.question_answering = QuestionAnswering()
        self.text_classification = TextClassification()
        self.token_classification = TokenClassification()


_model = StatelessModel()


def question_answering_preprocess(inputs: Input):
    return _model.question_answering.preprocess(inputs)


def question_answering_postprocess(inputs: Input):
    return _model.question_answering.postprocess(inputs)


def text_classification_preprocess(inputs: Input):
    return _model.text_classification.preprocess(inputs)


def text_classification_postprocess(inputs: Input):
    return _model.text_classification.postprocess(inputs)


def token_classification_preprocess(inputs: Input):
    return _model.token_classification.preprocess(inputs)


def token_classification_postprocess(inputs: Input):
    return _model.token_classification.postprocess(inputs)


def handle(inputs: Input):
    # This model only support pre-processing and post-processing
    return None
