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

from djl_python import Input
from transformers import AutoTokenizer


class BaseNLPModel(object):

    def __init__(self, application):
        settings_file = application + "_config.json"
        with open(settings_file) as f:
            config = json.load(f)

        mapping_file = application + "_labels.json"
        if os.path.isfile(mapping_file):
            with open(mapping_file) as f:
                self.mapping = json.load(f)

        self.max_length = int(config["max_length"])
        self.do_lower_case = config["do_lower_case"]
        self.model_name = config["model_name"]
        self.tokenizer = AutoTokenizer.from_pretrained(
            self.model_name,
            do_lower_case=self.do_lower_case
        )

    def preprocess(self, data: Input):
        pass

    def postprocess(self, data: Input):
        pass
