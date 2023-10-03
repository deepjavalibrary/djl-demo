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

instance_ondemand_pricing = {
    "p3.2xlarge": 3.06,
    "p3.8xlarge": 12.24,
    "p3.16xlarge": 24.48,
    "g4dn.2xlarge": 0.452,
    "g4dn.12xlarge": 2.348,
    "g5.2xlarge": 1.212,
    "g5.12xlarge": 5.672,
    "g5.48xlarge": 16.288,
    "p4d.24xlarge": 32.77,
    "p4de.24xlarge": 40.96,
    "p5.48xlarge": 98.32,
    "inf2.xlarge": 0.76,
    "inf2.24xlarge": 6.49,
    "inf2.48xlarge": 12.98,
    "trn1.2xlarge": 1.34,
    "trn1.32xlarge": 21.50,
}


def filter_instance_series(instances, series=None):
    if series is None:
        series = ["p3", "p5"]
    filtered_instances = []
    for instance_option in instances:
        should_remove = False
        for non_consider_instance in series:
            if instance_option['name'].startswith(non_consider_instance):
                should_remove = True
                break
        if not should_remove:
            filtered_instances.append(instance_option)
    return filtered_instances


def find_cheapest_instance(instances):
    cheapest_price = 10000.0
    selection = {}
    for instance_option in instances:
        if instance_ondemand_pricing[instance_option['name']] < cheapest_price:
            selection = instance_option
            cheapest_price = instance_ondemand_pricing[instance_option['name']]
    return selection


def find_best_fit_model_instance(instances, remaining_memory_percent=0.5):
    # Get all instances that offer 50% more memory
    filtered_instances = []
    for instance in instances:
        select = False
        configs = []
        for config in instance['lmi_configs']:
            if 1 - config['memory_pressure'] > remaining_memory_percent:
                select = True
                configs.append(config)
        if select:
            instance['lmi_configs'] = configs
            filtered_instances.append(instance)
    cheapest_acc = 10000.0
    selection = {}
    # find cheapest accelerator cost
    for instance in filtered_instances:
        per_acc_cost = instance_ondemand_pricing[instance['name']] / instance['num_acc']
        for config in instance['lmi_configs']:
            if config['serving_properties']['option.tensor_parallel_degree'] * per_acc_cost < cheapest_acc:
                cheapest_acc = config['serving_properties']['option.tensor_parallel_degree'] * per_acc_cost
                selection = instance
    return selection


def find_best_performance(instances):
    considered_instances = ["g5.2xlarge", "p4d.24xlarge", "p4de.24xlarge", "trn1.2xlarge", "trn1.32xlarge"]
    for considered_instance in considered_instances:
        for instance in instances:
            if instance['name'] == considered_instance:
                return instance
    return None


def best_latency_chooser(lmi_configs):
    tp = 16
    selection = {}
    for config in lmi_configs:
        if config['serving_properties']['option.tensor_parallel_degree'] < tp:
            tp = config['serving_properties']['option.tensor_parallel_degree']
            selection = config
    return selection


def best_throughput_chooser(lmi_configs, num_acc):
    batch_size = 0
    selection = {}
    max_tp = 16
    for config in lmi_configs:
        tp = config['serving_properties']['option.tensor_parallel_degree']
        model_copies = num_acc / tp
        max_batch_size = model_copies * config['serving_properties']['option.max_rolling_batch_size']
        if max_batch_size > batch_size:
            batch_size = max_batch_size
            selection = config
            max_tp = tp
        elif max_batch_size == batch_size:
            if tp < max_tp:
                selection = config
    return selection


def combine_chooser(lmi_configs, num_acc):
    latency_config = best_latency_chooser(lmi_configs)
    throughput_config = best_throughput_chooser(lmi_configs, num_acc)
    if latency_config == throughput_config:
        return {'latency_throughput': latency_config}
    result = {'latency': latency_config, 'throughput': throughput_config}
    return result


def combine_instances(instances):
    cheap_instance = find_cheapest_instance(instances)
    best_fit_instance = find_best_fit_model_instance(instances)
    performant_instance = find_best_performance(instances)
    result = {cheap_instance['name']: {'advantage': ['cheap'], 'instance': cheap_instance}}
    if best_fit_instance['name'] in result.keys():
        result[best_fit_instance['name']]['advantage'].append('best_fit')
    else:
        result[best_fit_instance['name']] = {'advantage': ['best_fit'], 'instance': best_fit_instance}
    if performant_instance['name'] in result.keys():
        result[performant_instance['name']]['advantage'].append('performant')
    else:
        result[performant_instance['name']] = {'advantage': ['performant'], 'instance': performant_instance}
    for key in result.keys():
        result[key]['lmi_configs'] = combine_chooser(result[key]['instance']['lmi_configs'], result[key]['instance']['num_acc'])
        del result[key]['instance']
    return result


def decision_maker(recommendation):
    # GPU recommendation
    gpu_recommendation = recommendation.copy()
    gpu_recommendation['instances'] = filter_instance_series(gpu_recommendation['instances'],
                                                             ['p3', 'p5', 'inf2', 'trn1'])
    neuron_recommendation = recommendation.copy()
    neuron_recommendation['instances'] = filter_instance_series(neuron_recommendation['instances'],
                                                                ['p3', 'p4', 'p5', 'g4', 'g5'])
    if gpu_recommendation['instances']:
        gpu_recommendation['instances'] = combine_instances(gpu_recommendation['instances'])
    if neuron_recommendation['instances']:
        neuron_recommendation['instances'] = combine_instances(neuron_recommendation['instances'])
    return gpu_recommendation, neuron_recommendation


def decision_pretty_print(decisions):
    msg_str = ["**** IR recommendation summary ****",
               f"Model: {decisions['model_id']} generated based on category {decisions['category']},",
               f"Size {decisions['size']}GB Precision {decisions['dtype']}:"]
    if not decisions['instances']:
        msg_str.append("No avaiable instances to run with!")
        print('\n'.join(msg_str))
        return
    for instance_name, configs in decisions['instances'].items():
        msg_str.append(f"  {','.join(configs['advantage'])} instance recommendations {instance_name}")
        for lmi_adv, settings in configs['lmi_configs'].items():
            msg_str.append(f"    For best {lmi_adv}: {settings}")
    print('\n'.join(msg_str))
