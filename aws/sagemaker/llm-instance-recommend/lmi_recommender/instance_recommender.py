#!/usr/bin/env python
#
# Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
# except in compliance with the License. A copy of the License is located at
#
# http://aws.amazon.com/apache2.0/
#
# or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for
# the specific language governing permissions and limitations under the License.
import os
import subprocess
from typing import List

import requests
import logging

model_category = {
    "llama-70b": {
        "identifier": {"architectures": ["LlamaForCausalLM"], "intermediate_size": 28672, "num_attention_heads": 64,
                       "num_hidden_layers": 80},
        "model_info": {"size": 134.0, "dtype": "fp16"}},
    "llama-13b": {
        "identifier": {"architectures": ["LlamaForCausalLM"], "intermediate_size": 13824, "num_attention_heads": 40,
                       "num_hidden_layers": 40},
        "model_info": {"size": 26.0, "dtype": "fp16"}},
    "llama-7b": {
        "identifier": {"architectures": ["LlamaForCausalLM"], "intermediate_size": 11008, "num_attention_heads": 32,
                       "num_hidden_layers": 32},
        "model_info": {"size": 14.0, "dtype": "fp16"}},
    "llama-34b": {
        "identifier": {"architectures": ["LlamaForCausalLM"], "intermediate_size": 22016, "num_attention_heads": 64,
                       "num_hidden_layers": 48, "num_key_value_heads": 8},
        "model_info": {"size": 68.0, "dtype": "fp16"}},
    "falcon-7b": {"identifier": {"architectures": ["FalconForCausalLM"], "hidden_size": 4544, "n_head": 71, "n_layer": 32},
                  "model_info": {"size": 16.0, "dtype": "fp16"}},
    "falcon-40b": {
        "identifier": {"architectures": ["FalconForCausalLM"], "hidden_size": 8192, "n_head": 128, "n_layer": 60},
        "model_info": {"size": 84.0, "dtype": "fp16"}},
    "falcon-180b": {
        "identifier": {"architectures": ["FalconForCausalLM"], "hidden_size": 14848, "num_attention_heads": 232,
                       "num_hidden_layers": 80},
        "model_info": {"size": 360.0, "dtype": "fp16"}},
    "flan-t5-xl": {"identifier": {"architectures": ["T5ForConditionalGeneration"], "d_ff": 5120, "d_kv": 64,
                                  "num_decoder_layers": 24, "num_heads": 32, "num_layers": 24},
                   "model_info": {"size": 6.0, "dtype": "fp16"}},
    "flan-t5-xxl": {"identifier": {"architectures": ["T5ForConditionalGeneration"], "d_ff": 10240, "d_kv": 64,
                                   "num_decoder_layers": 24, "num_heads": 64, "num_layers": 24},
                    "model_info": {"size": 46.0, "dtype": "fp16"}},
    "flan-ul2": {"identifier": {"architectures": ["T5ForConditionalGeneration"], "d_ff": 16384, "d_kv": 256,
                                "num_decoder_layers": 32, "num_heads": 16, "num_layers": 32},
                 "model_info": {"size": 40.0, "dtype": "fp16"}},
    "gptneox-20b": {
        "identifier": {"architectures": ["GPTNeoXForCausalLM"], "hidden_size": 6144, "intermediate_size": 24576,
                       "num_attention_heads": 64, "num_hidden_layers": 44},
        "model_info": {"size": 42.0, "dtype": "fp16"}},
    "gptneox-12b": {
        "identifier": {"architectures": ["GPTNeoXForCausalLM"], "hidden_size": 5120, "intermediate_size": 20480,
                       "num_attention_heads": 40, "num_hidden_layers": 36},
        "model_info": {"size": 24.0, "dtype": "fp16"}},
    "gptneox-6.9b": {
        "identifier": {"architectures": ["GPTNeoXForCausalLM"], "hidden_size": 4096, "intermediate_size": 16384,
                       "num_attention_heads": 32, "num_hidden_layers": 32},
        "model_info": {"size": 14.0, "dtype": "fp16"}},
    "chatglm-6b": {"identifier": {"architectures": ["ChatGLMModel"], "hidden_size": 4096, "num_attention_heads": 32,
                                  "num_layers": 28},
                   "model_info": {"size": 13.0, "dtype": "fp16"}},
    "mpt-7b": {"identifier": {"architectures": ["MPTForCausalLM"], "d_model": 4096, "n_heads": 32, "num_layers": 32},
               "model_info": {"size": 14.0, "dtype": "fp16"}},
    "mpt-30b": {"identifier": {"architectures": ["MPTForCausalLM"], "d_model": 7168, "n_heads": 64, "num_layers": 48},
                "model_info": {"size": 60.0, "dtype": "fp16"}},
    "bigcode-star": {
        "identifier": {"architectures": ["GPTBigCodeForCausalLM"], "n_embd": 6144, "n_heads": 48, "num_layers": 40},
        "model_info": {"size": 63.0, "dtype": "fp16"}},
}

general_supported_arch = {
    "GPTBigCodeForCausalLM": 'bigcode',
    "MPTForCausalLM": 'mpt',
    "GPTNeoXForCausalLM": 'gptneox',
    "T5ForConditionalGeneration": 'flan-t5',
    "FalconForCausalLM": 'falcon',
    "LlamaForCausalLM": 'llama',
    "GPTJForCausalLM": 'gptj',
    "OPTForCausalLM": 'opt',
    "BloomForCausalLM": 'bloom',
    "ChatGLMModel": 'chatglm',
    "BaiChuanForCausalLM": "baichuan",
    "InternLMForCausalLM": "internlm",
    "MistralForCausalLM": "mistral"
}

neuron_supported_arch = {
    'llama', 'opt', 'gptj', 'gptneox', 'bloom'
}

instances = {
    "p3.2xlarge": {"memory": 16, "num_acc": 1},
    "p3.8xlarge": {"memory": 16, "num_acc": 4},
    "p3.16xlarge": {"memory": 16, "num_acc": 8},
    "g4dn.2xlarge": {"memory": 16, "num_acc": 1},
    "g4dn.12xlarge": {"memory": 16, "num_acc": 4},
    "g5.2xlarge": {"memory": 24, "num_acc": 1},
    "g5.12xlarge": {"memory": 24, "num_acc": 4},
    "g5.48xlarge": {"memory": 24, "num_acc": 8},
    "p4d.24xlarge": {"memory": 40, "num_acc": 8},
    "p4de.24xlarge": {"memory": 80, "num_acc": 8},
    "p5.48xlarge": {"memory": 80, "num_acc": 8},
    "inf2.xlarge": {"memory": 16, "num_acc": 2},
    "inf2.24xlarge": {"memory": 16, "num_acc": 12},
    "inf2.48xlarge": {"memory": 16, "num_acc": 24},
    "trn1.2xlarge": {"memory": 16, "num_acc": 2},
    "trn1.32xlarge": {"memory": 16, "num_acc": 32},
}


def fetch_config_json(model_id: str):
    try:
        url = f"https://huggingface.co/{model_id}/raw/main/config.json"
        headers = {'Accept': 'application/json'}
        config = requests.get(url, headers=headers).json()
        if "architectures" not in config:
            raise ValueError(f"config {config} does not have architecture")
        return config
    except Exception as e:
        logging.warning(f"could not fetch {model_id} for its config.json", e)
        return None


# TODO: https://huggingface.co/docs/accelerate/main/en/usage_guides/model_size_estimator
def size_collector(lines: List[str], target='.safetensors'):
    sizes = []
    for line in lines:
        if target in line:
            result = line[line.find("(") + 1:line.find(")")].split()
            size, label = float(result[0]), result[1]
            if label == 'MB':
                size *= 0.001
                label = 'GB'
            if label == 'GB':
                sizes.append(size)
    return sizes


def model_size_grabber(model_id: str):
    subprocess.run(['git', 'clone', '--no-checkout', f'https://huggingface.co/{model_id}', 'test'])
    result = os.popen('cd test && git lfs ls-files -s').read()
    subprocess.run(f'rm -rf test'.split())
    result = result.split('\n')
    sizes = size_collector(result)
    if not sizes:
        sizes = size_collector(result, '.bin')
    final_sizes = sum(sizes)
    if final_sizes == 0:
        return None
    return final_sizes


def raw_model_matcher(model_id: str):
    config = fetch_config_json(model_id)
    category = None
    if len(config['architectures']) == 1 and config['architectures'][0] in general_supported_arch.keys():
        category = general_supported_arch[config['architectures'][0]]
    dtype = config.get('torch_dtype', 'float32')
    size = model_size_grabber(model_id)
    if category is None or size is None:
        return None, None
    if dtype == 'float32':
        size /= 2.0
    elif dtype == 'float16' or dtype == 'bfloat16':
        pass
    else:
        raise ValueError(f"Unsupported datatype: {dtype}")
    return category, {"model_info": {"size": size, 'dtype': "fp16"}}


def match_identifier(identifier: dict, candidate: dict):
    for key, value in identifier.items():
        if key not in candidate or value != candidate[key]:
            return False
    return True


def model_id_to_category(model_id: str):
    config = fetch_config_json(model_id)
    for key, value in model_category.items():
        if match_identifier(value["identifier"], config):
            return key, value.copy()
    return None, None


def instance_matcher(model_info, instance_name, instance_data, category, preserved_percentage=0.10):
    if instance_name.startswith('inf2') or instance_name.startswith('trn1'):
        found = False
        for arch in neuron_supported_arch:
            if category.startswith(arch):
                found = True
        if not found:
            return False

    available_mem = instance_data["memory"] * instance_data["num_acc"] * (1 - preserved_percentage)
    if available_mem > model_info["size"]:
        return True
    return False


def instance_recommendation(model_id: str, dtype='fp16'):
    category, config = model_id_to_category(model_id)
    if not category:
        category, config = raw_model_matcher(model_id)
    if not category:
        return {"model_id": model_id, "category": "unknown", "instances": []}
    final_result = {"model_id": model_id, "category": category, "instances": []}
    if dtype == 'int8':
        config['model_info']['size'] /= 2
        config['model_info']['dtype'] = 'int8'
    final_result.update(config['model_info'])
    for instance_name, instance_data in instances.items():
        if instance_matcher(config["model_info"], instance_name, instance_data, category):
            updated_data = instance_data.copy()
            updated_data.update({"name": instance_name})
            final_result["instances"].append(updated_data)
    return final_result
