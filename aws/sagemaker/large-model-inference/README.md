## SageMaker sample notebook for LLM

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
- [Flan-UL2](sample-llm/rollingbatch_deploy_flan_ul2.ipynb)
- [MPT-30B](sample-llm/rollingbatch_deploy_mpt_30b.ipynb)
- [Octocoder](sample-llm/rollingbatch_deploy_octocoder_w_pagedattn.ipynb)
- [CodeGen 2.5](sample-llm/rollingbatch_deploy_codegen25_7b.ipynb)
- [Falcon-40B](sample-llm/rollingbatch_deploy_falcon_40b.ipynb)
- [CodeLLAMA-34B](sample-llm/rollingbatch_deploy_codellama_34b.ipynb)

### FasterTransformer

- [OpenAssistant GPTNeoX](sample-llm/fastertransformer_deploy_pythia12b_triton_mode.ipynb)

### VLLM Rolling Batch

- [LLAMA-13B](sample-llm/vllm_deploy_llama_13b.ipynb)

### Neuron inference

- [OPT RollingBatch](sample-llm/tnx_rollingbatch_deploy_opt.ipynb)
- [LLAMA-13B RollingBatch](sample-llm/tnx_rollingbatch_deploy_llama_13b.ipynb)
- [LLAMA-7B Int8 RollingBatch](sample-llm/tnx_rollingbatch_deploy_llama_7b_int8.ipynb)
