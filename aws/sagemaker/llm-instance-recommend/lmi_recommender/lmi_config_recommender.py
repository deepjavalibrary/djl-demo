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

rolling_batch_fallback = {
    'strategy': {0.1: {"option.max_rolling_batch_size": 4},
                 0.3: {"option.max_rolling_batch_size": 8},
                 0.5: {"option.max_rolling_batch_size": 32},
                 0.7: {"option.max_rolling_batch_size": 64}
                 },
    'fp16': {
        "engine": "Python",
        "option.dtype": "fp16",
        "option.device_map": "auto",
        "option.rolling_batch": "auto"
    },
    'int8': {
        "engine": "Python",
        "option.dtype": "fp16",
        "option.device_map": "auto",
        "option.rolling_batch": "auto",
        "option.load_in_8bit": "true"
    }
}

tnx_rolling_batch = {
    'strategy': {0.1: {"option.max_rolling_batch_size": 4, "option.batch_size": 4},
                 0.3: {"option.max_rolling_batch_size": 8, "option.batch_size": 8},
                 0.5: {"option.max_rolling_batch_size": 32, "option.batch_size": 32},
                 0.7: {"option.max_rolling_batch_size": 64, "option.batch_size": 64}
                 },
    'fp16': {
        "engine": "Python",
        "option.entryPoint": "djl_python.transformers_neuronx",
        "option.dtype": "fp16",
        "option.n_positions": 2048,
        "option.rolling_batch": "auto"
    },
    # TODO: refine INT8 option
    'int8': {
        "engine": "Python",
        "option.entryPoint": "djl_python.transformers_neuronx",
        "option.dtype": "fp16",
        "option.rolling_batch": "auto",
        "option.load_in_8bit": "true"
    }
}

lmi_dist_rolling_batch = {
    'flash1': {
        'model_category': ['llama-13b', 'llama-7b', 'falcon-7b', 'falcon-40b', 'flan-t5', 'gptneox', 'mpt', 'bigcode'],
        'instance': ['g4', 'g5', 'p4', 'p5']},
    'flash2': {'model_category': {'llama', 'falcon', 'flan-t5', 'gptneox', 'mpt', 'bigcode'},
               'instance': ['g5', 'p4', 'p5']},
    'strategy': {0.1: {"option.max_rolling_batch_size": 4,
                       "option.max_rolling_batch_prefill_tokens": 1560},
                 0.3: {"option.max_rolling_batch_size": 8,
                       "option.max_rolling_batch_prefill_tokens": 8196},
                 0.5: {"option.max_rolling_batch_size": 32,
                       "option.max_rolling_batch_prefill_tokens": 16000},
                 0.7: {"option.max_rolling_batch_size": 64,
                       "option.max_rolling_batch_prefill_tokens": 32768},
                 0.85: {"option.max_rolling_batch_size": 128,
                        "option.max_rolling_batch_prefill_tokens": 65536}},
    'fp16': {
        "engine": "MPI",
        "option.dtype": "fp16",
        "option.rolling_batch": "auto"
    },
    'int8': {
        "engine": "MPI",
        "option.dtype": "fp16",
        "option.rolling_batch": "auto",
        "option.quantize": "bitsandbytes"
    }
}

vllm_rolling_batch = {
    'vllm': {
        'model_category': {'llama', 'falcon', 'flan-t5', 'gptneox', 'mpt', 'bigcode', 'chatglm', 'baichuan', 'internlm',
                           'mistral'},
        'instance': ['g5', 'p4', 'p5']},
    'strategy': {0.1: {"option.max_rolling_batch_size": 4},
                 0.3: {"option.max_rolling_batch_size": 8},
                 0.5: {"option.max_rolling_batch_size": 32},
                 0.7: {"option.max_rolling_batch_size": 64},
                 0.85: {"option.max_rolling_batch_size": 128}},
    'fp16': {
        "engine": "Python",
        "option.dtype": "fp16",
        "option.rolling_batch": "vllm"
    }
}


def category_checker(target, category):
    found = False
    for item in category:
        if target.startswith(item):
            found = True
            break
    return found


def rolling_batch_chooser(model_category: str, instance: dict, dtype: str):
    instance_name = instance['name']
    if instance_name.startswith('inf2') or instance_name.startswith('trn1'):
        return 'neuronx'
    # find Flash 2 supported model
    flash2_checker = lmi_dist_rolling_batch['flash2']
    if category_checker(model_category, flash2_checker['model_category']):
        if category_checker(instance['name'], flash2_checker['instance']):
            return 'flash2'
    # find vllm supported model
    vllm_checker = vllm_rolling_batch['vllm']
    if category_checker(model_category, vllm_checker['model_category']):
        if category_checker(instance['name'], vllm_checker['instance']) and dtype == 'fp16':
            return 'vllm'
    # find Flash 1 supported model
    flash1_checker = lmi_dist_rolling_batch['flash1']
    if category_checker(model_category, flash1_checker['model_category']):
        if category_checker(instance['name'], flash1_checker['instance']):
            return 'flash1'
    return 'fallback'


def memory_prealloc_chooser(model_size, instance_info):
    tp_sizes = [1, 2, 4, 8]
    if instance_info['name'].startswith('inf2'):
        # https://awsdocs-neuron.readthedocs-hosted.com/en/latest/libraries/transformers-neuronx/transformers-neuronx-developer-guide.html#tensor-parallelism-support
        tp_sizes = [1, 2, 4, 8, 12, 24]
    elif instance_info['name'].startswith('trn1'):
        tp_sizes = [1, 2, 8, 16, 32]
    tp_sizes = filter(lambda x: x <= instance_info['num_acc'], tp_sizes)
    result = []
    for tp_size in tp_sizes:
        total_memory = tp_size * instance_info['memory']
        if model_size < total_memory:
            remained_memory = (total_memory - model_size) * 1.0 / total_memory
            result.append({remained_memory: tp_size})
    return result


def lmi_config_recommender(instance_recommendation: dict):
    dtype = instance_recommendation['dtype']
    for instance in instance_recommendation['instances']:
        result = []
        strategy = rolling_batch_chooser(instance_recommendation['category'], instance, dtype)
        tp_memories = memory_prealloc_chooser(instance_recommendation['size'], instance)
        if 'flash2' == strategy or 'flash1' == strategy:
            rolling_batch = lmi_dist_rolling_batch
        elif 'neuronx' == strategy:
            rolling_batch = tnx_rolling_batch
        elif 'vllm' == strategy:
            rolling_batch = vllm_rolling_batch
        else:
            rolling_batch = rolling_batch_fallback
        remained_memory_ratios = sorted(rolling_batch['strategy'].keys(), reverse=True)
        for tp_memory in tp_memories:
            memory, tp = list(tp_memory.items())[0]
            for ratio in remained_memory_ratios:
                serving_properties = rolling_batch[dtype].copy()
                if memory > ratio:
                    concurrency_settings = rolling_batch['strategy'][ratio].copy()
                    if 'flash1' == strategy:
                        concurrency_settings.pop('option.max_rolling_batch_prefill_tokens')
                        concurrency_settings['option.paged_attention'] = "false"
                    serving_properties.update(concurrency_settings)
                    serving_properties['option.tensor_parallel_degree'] = tp
                    serving_properties['option.model_id'] = instance_recommendation['model_id']
                    model_memory = 1 - memory
                    result.append({'memory_pressure': model_memory, "serving_properties": serving_properties,
                                   'strategy': strategy})
                    # only keep 1 selection per memory pressure
                    break
        instance['lmi_configs'] = result


def lmi_pretty_print(recommendation):
    msg_str = ["**** LMI config recommendation summary ****",
               f"Model: {recommendation['model_id']} generated based on category {recommendation['category']},",
               f"Size {recommendation['size']}GB Precision {recommendation['dtype']}:"]
    for instance in recommendation['instances']:
        msg_str.append(f"  {instance['name']} instance recommendations")
        for config in instance['lmi_configs']:
            memory_pressure, serving_properties = config['memory_pressure'], config['serving_properties']
            msg_str.append(f"    Model Memory size {int(memory_pressure * 100)}%: {serving_properties}")
    print('\n'.join(msg_str))
