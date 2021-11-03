import logging
import os
import sys

import torch
import torch_neuron
import transformers
from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification,
    AutoModelForQuestionAnswering,
    AutoModelForTokenClassification
)

# Enable logging so we can see any important warnings
logger = logging.getLogger('Neuron')
logger.setLevel(logging.INFO)


# compile model to use one neuron core only
# num_cores = 4  # for inf1.xl and inf1.2xl, this value should be 16 on inf1.15xl
# nc_env = ','.join(['1'] * num_cores)
# os.environ['NEURONCORE_GROUP_SIZES'] = nc_env


def transformers_model_downloader(app):
    model_file = os.path.join(app, app + ".pt")
    if os.path.isfile(model_file):
        print("model already downloaded: " + model_file)
        return

    print("Download model for: ", model_file)
    if app == "text_classification":
        model_name = "bert-base-uncased"
        max_length = 150
        model = AutoModelForSequenceClassification.from_pretrained(model_name, torchscript=True, num_labels=2)
        tokenizer = AutoTokenizer.from_pretrained(model_name, do_lower_case=True)
    elif app == "question_answering":
        model_name = "distilbert-base-uncased-distilled-squad"
        max_length = 128
        model = AutoModelForQuestionAnswering.from_pretrained(model_name, torchscript=True)
        tokenizer = AutoTokenizer.from_pretrained(model_name, do_lower_case=True)
    elif app == "token_classification":
        model_name = "bert-base-uncased"
        max_length = 150
        model = AutoModelForTokenClassification.from_pretrained(model_name, torchscript=True, num_labels=9)
        tokenizer = AutoTokenizer.from_pretrained(model_name, do_lower_case=True)
    else:
        print("Unknown application: " + app)
        return

    text = "How is the weather"
    paraphrase = tokenizer.encode_plus(text,
                                       max_length=max_length,
                                       truncation=True,
                                       padding='max_length',
                                       add_special_tokens=True,
                                       return_tensors='pt')
    example_inputs = paraphrase['input_ids'], paraphrase['attention_mask']

    traced_model = torch.neuron.trace(model, example_inputs)

    # Export to saved model
    os.makedirs(app, exist_ok=True)
    traced_model.save(model_file)

    tokenizer.save_pretrained(app)

    logging.info("Compile model %s success.", app)


if __name__ == "__main__":
    logging.basicConfig(stream=sys.stdout,
                        format="%(message)s",
                        level=logging.INFO)
    logging.info("Transformers version: %s", transformers.__version__)

    transformers_model_downloader("question_answering")
    # transformers_model_downloader("text_classification")
    # transformers_model_downloader("token_classification")

