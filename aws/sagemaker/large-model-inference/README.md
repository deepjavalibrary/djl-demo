# SageMaker Sample Notebooks for LLM

In this section, we provide some sample instruction to use LMI container on SageMaker.

- [Bring-Your-Own-Container template](BYOC_template_with_LMI_solution.ipynb)
- [Standard model.py template](standard_template_with_LMI_solution.ipynb)
- [LMI PySDK template](pysdk_template_with_LMI_solution.ipynb)

For the list of LMI containers that is on DLC, please click [here](https://github.com/aws/deep-learning-containers/blob/master/available_images.md#large-model-inference-containers).

For the list of available BYOC containers, please clck [here](https://hub.docker.com/r/deepjavalibrary/djl-serving/tags).

For more information on LMI documentation on SageMaker, click [here](https://docs.aws.amazon.com/sagemaker/latest/dg/realtime-endpoints-large-model-inference.html).

For all the serving.prorperties options you could set on DJLServing, click [here](https://docs.djl.ai/docs/serving/serving/docs/modes.html#servingproperties).

## Sample notebooks

### LMI_Dist Rolling Batch

- [LLAMA2-70B](sample-llm/rollingbatch_deploy_llama2_70b_w_pagedattn.ipynb)
- [LLAMA 7B rolling batch with customized model.py](sample-llm/rollingbatch_llama_7b_customized_preprocessing.ipynb)
- [LLAMA 7B rolling batch with stop reasoning](sample-llm/rollingbatch_llama_7b_stop_reason.ipynb)
- [Flan-UL2](sample-llm/rollingbatch_deploy_flan_ul2.ipynb)
- [MPT-30B](sample-llm/rollingbatch_deploy_mpt_30b.ipynb)
- [Octocoder](sample-llm/rollingbatch_deploy_octocoder_w_pagedattn.ipynb)
- [CodeGen 2.5](sample-llm/rollingbatch_deploy_codegen25_7b.ipynb)
- [Falcon-40B](sample-llm/rollingbatch_deploy_falcon_40b.ipynb)
- [CodeLLAMA-34B](sample-llm/rollingbatch_deploy_codellama_34b.ipynb)
- [LLAMA2-13B-GPTQ](sample-llm/rollingbatch_deploy_llama2-13b-gptq.ipynb)
- [LLAMA2-70B-GPTQ](sample-llm/rollingbatch_deploy_llama2-70b-gptq.ipynb)

### HF Acc Rolling Batch
- [LLAMA2-70B](sample-llm/hf_acc_deploy_llama2_70b.ipynb)
- [LLAMA 7B rolling batch with customized model.py](sample-llm/hf_acc_deploy_llama_7b_customized_preprocessing.ipynb)
- [Mistral 7B](sample-llm/hf_acc_deploy_mistral_7b.ipynb)
- [Falcon-40B](sample-llm/hf_acc_deploy_falcon_40b.ipynb)
- [CodeLLAMA-34B](sample-llm/hf_acc_deploy_codellama_34b.ipynb)
- [LLAMA2-13B-GPTQ](sample-llm/hf_acc_deploy_llama2-13b-gptq.ipynb)

### DeepSpeed

- [LLAMA-2-13B-SmoothQuant](sample-llm/ds_deploy_llama2-13b-smoothquant.ipynb)
- [LLAMA-2-13B-RollingBatch-SmoothQuant](sample-llm/ds_rollingbatch_deploy_llama2-13b-smoothquant.ipynb)
- [GPT-NeoX-20B-RollingBatch-SmoothQuant](sample-llm/ds_rollingbatch_deploy_gpt-neox-20b-smoothquant.ipynb)

### FasterTransformer

- [OpenAssistant GPTNeoX](sample-llm/fastertransformer_deploy_pythia12b_triton_mode.ipynb)

### VLLM Rolling Batch

- [LLAMA-13B](sample-llm/vllm_deploy_llama_13b.ipynb)
- [Mistral-7B](sample-llm/vllm_deploy_mistral_7b.ipynb)

### Neuron inference

- [OPT RollingBatch](sample-llm/tnx_rollingbatch_deploy_opt.ipynb)
- [LLAMA-13B RollingBatch](sample-llm/tnx_rollingbatch_deploy_llama_13b.ipynb)
- [LLAMA-7B Int8 RollingBatch](sample-llm/tnx_rollingbatch_deploy_llama_7b_int8.ipynb)

### TensorRT-LLM Rolling Batch

- [LLAMA-13B RollingBatch](sample-llm/trtllm_rollingbatch_deploy_llama_13b.ipynb)

### Misc

- [Multi-LoRA Adapters](sample-llm/multi_lora_adapter_inference.ipynb)

### Amazon SageMaker Examples

[Source Repo](https://github.com/aws/amazon-sagemaker-examples/tree/main)

- [GPT-J 6B](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/deepspeed/GPT-J-6B_DJLServing_with_PySDK.ipynb)
- [Bloom 176B](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/nlp/realtime/llm/bloom_176b/djl_deepspeed_deploy.ipynb)
- [Dolly 12B DeepSpeed](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/deploy-dolly-12b/dolly-12b-deepspeed-sagemaker.ipynb)
- [OPT-30B](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/nlp/realtime/llm/opt30b/djl_deepspeed_deploy_opt30b_no_custom_inference_code.ipynb)
- [OpenChatKit](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab4-openchatkit/deploy_openchatkit_on_sagemaker.ipynb)
- [GPT4AllJ](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab8-Inferentia2-gpt4all-j/inferentia2-llm-GPT4allJ.ipynb)
- [GPT NeoX HF Acc](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab3-optimize-llm/djl_accelerate_deploy_g5_12x_GPT_NeoX.ipynb)
- [GPT NeoX DeepSpeed](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab3-optimize-llm/g5_24xlarge/djl_deepspeed_deploy_GPT_NeoX.ipynb)
- [Flan-XXL fastertransformer](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab5-flan-t5-xxl/flan-xxl-sagemaker-fastertransformer-smaster.ipynb)
- [FlanT5 XXL FasterTransformer](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab5-flan-t5-xxl/flant5-xxl-fastertransformer-no-code.ipynb)
- [Flan UL2](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/flan-ul2-pySDK/flan-ul2-pySDK.ipynb)
- [GPT-J-6B Model Parallel Inference](https://github.com/aws/amazon-sagemaker-examples/blob/master/advanced_functionality/pytorch_deploy_large_GPT_model/GPT-J-6B-model-parallel-inference-DJL.ipynb)
- [Open LLama 7B](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab10-open-llama/open-llama-7b/open_llama_7b.ipynb)
- [LLama2 7B](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab11-llama2/meta-llama-2-7b-lmi.ipynb)
- [LLama2 7B Batching Throughput](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/llama2-7b-batching-throughput/llama2-7b-batching-throughput.ipynb)
- [LLama2 13B](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab11-llama2/meta-llama-2-13b-lmi.ipynb)
- [LLama2 70B](https://github.com/aws/amazon-sagemaker-examples/blob/master/inference/generativeai/llm-workshop/lab11-llama2/meta-llama-2-70b-lmi.ipynb)
