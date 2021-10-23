/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.examples;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.modality.nlp.qa.QAInput;
import ai.djl.ndarray.BytesSupplier;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HybridPythonEngine {

    private static final Logger logger = LoggerFactory.getLogger(HybridPythonEngine.class);

    private HybridPythonEngine() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        Criteria<Input, Output> criteria =
                Criteria.builder()
                        .setTypes(Input.class, Output.class)
                        .optModelPath(Paths.get("models/hybrid"))
                        .optEngine("Python") // Use Python engine for pre/post processing
                        .build();

        // The Python model is implemented in stateless way, it can be shared
        try (ZooModel<Input, Output> python = criteria.loadModel();
                Predictor<Input, Output> transformer = python.newPredictor()) {
            textClassificationExample(transformer);
            questionAnsweringExample(transformer);
            tokenClassificationExample(transformer);
        }
    }

    private static void textClassificationExample(Predictor<Input, Output> transformer)
            throws IOException, ModelException, TranslateException {
        Criteria<NDList, NDList> bertCriteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelPath(Paths.get("models/hybrid/text_classification.pt"))
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();

        try (ZooModel<NDList, NDList> bert = bertCriteria.loadModel();
                Predictor<NDList, NDList> predictor = bert.newPredictor()) {
            Input input = new Input();
            input.add("Bloomberg has decided to publish a new report on the global economy.");
            input.addProperty("handler", "text_classification_preprocess");
            Output output = transformer.predict(input);

            NDManager manager = bert.getNDManager().newSubManager(); // use pytorch engine
            NDList ndList = output.getDataAsNDList(manager);
            NDList predictions = predictor.predict(ndList);

            Path file = Paths.get("build/text_classification_output.ndlist");
            try (OutputStream os = Files.newOutputStream(file)) {
                predictions.encode(os);
            }

            // The following python post-processing is not necessary, just for demo purpose
            Input postProcessing = new Input();
            postProcessing.add(predictions);
            postProcessing.addProperty("handler", "text_classification_postprocess");
            output = transformer.predict(postProcessing);
            String result = output.getData().getAsString();

            logger.info(result);
        }
    }

    private static void questionAnsweringExample(Predictor<Input, Output> transformer)
            throws IOException, ModelException, TranslateException {
        Criteria<NDList, NDList> bertCriteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelPath(Paths.get("models/hybrid/question_answering.pt"))
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();

        try (ZooModel<NDList, NDList> bert = bertCriteria.loadModel();
                Predictor<NDList, NDList> predictor = bert.newPredictor()) {
            Input input = new Input();
            QAInput qa =
                    new QAInput("How is the weather", "he weather is nice, it is beautiful day");
            input.add(BytesSupplier.wrapAsJson(qa));
            input.addProperty("handler", "question_answering_preprocess");
            Output output = transformer.predict(input);

            NDManager manager = bert.getNDManager().newSubManager(); // use pytorch engine
            NDList ndList = output.getDataAsNDList(manager);
            NDList predictions = predictor.predict(ndList);

            Path file = Paths.get("build/question_answering_ids.ndlist");
            try (OutputStream os = Files.newOutputStream(file)) {
                new NDList(ndList.head()).encode(os);
            }
            file = Paths.get("build/question_answering_output.ndlist");
            try (OutputStream os = Files.newOutputStream(file)) {
                predictions.encode(os);
            }

            Input postProcessing = new Input();
            postProcessing.add("data", predictions);
            postProcessing.add("input_ids", new NDList(ndList.head()));
            postProcessing.addProperty("handler", "question_answering_postprocess");
            output = transformer.predict(postProcessing);
            String result = output.getData().getAsString();

            logger.info(result);
        }
    }

    private static void tokenClassificationExample(Predictor<Input, Output> transformer)
            throws IOException, ModelException, TranslateException {
        Criteria<NDList, NDList> bertCriteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelPath(Paths.get("models/hybrid/token_classification.pt"))
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();

        try (ZooModel<NDList, NDList> bert = bertCriteria.loadModel();
                Predictor<NDList, NDList> predictor = bert.newPredictor()) {
            Input input = new Input();
            input.add("Bloomberg has decided to publish a new report on the global economy.");
            input.addProperty("handler", "token_classification_preprocess");
            Output output = transformer.predict(input);

            NDManager manager = bert.getNDManager().newSubManager(); // use pytorch engine
            NDList ndList = output.getDataAsNDList(manager);
            NDList predictions = predictor.predict(ndList);

            Path file = Paths.get("build/token_classification_ids.ndlist");
            try (OutputStream os = Files.newOutputStream(file)) {
                new NDList(ndList.head()).encode(os);
            }
            file = Paths.get("build/token_classification_output.ndlist");
            try (OutputStream os = Files.newOutputStream(file)) {
                predictions.encode(os);
            }

            Input postProcessing = new Input();
            postProcessing.add("data", predictions);
            postProcessing.add("input_ids", new NDList(ndList.head()));
            postProcessing.addProperty("handler", "token_classification_postprocess");
            output = transformer.predict(postProcessing);
            String result = output.getData().getAsString();

            logger.info(result);
        }
    }
}
