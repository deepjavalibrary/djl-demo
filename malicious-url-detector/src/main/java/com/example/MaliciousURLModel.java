/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.example;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.convolutional.Conv1d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.Dropout;
import ai.djl.nn.pooling.Pool;
import ai.djl.translate.TranslateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Wraps Model object, definition. loading and training methods into a singleton object */
class MaliciousURLModel {

    private static final Logger logger = LoggerFactory.getLogger(MaliciousURLModel.class);

    private static final MaliciousURLModel INSTANCE = new MaliciousURLModel();

    private String modelName;
    private String modelDir;

    Model getModel() {
        return model;
    }

    private Model model;

    private MaliciousURLModel() {
        modelName = "maliciousURLCNNModel";
        modelDir = "model";
    }

    /**
     * Define the imperative model, returns a model object will all the layers sets model object to
     * the blocks Used for Bot training and inference.
     */
    void defineModel() {
        SequentialBlock block = new SequentialBlock();
        float dropoutProbability = (float) 0.5;
        int fullyConnected = 1024;
        int numberOfFilters = 256;
        block.add(Conv1d.builder().setKernelShape(new Shape(7)).setFilters(numberOfFilters).build())
                .add(Activation.reluBlock())
                .add(Pool.maxPool1dBlock(new Shape(3), new Shape(3), new Shape(0)))
                .add(
                        Conv1d.builder()
                                .setKernelShape(new Shape(7))
                                .setFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(Pool.maxPool1dBlock(new Shape(3), new Shape(3), new Shape(0)))
                .add(
                        Conv1d.builder()
                                .setKernelShape(new Shape(3))
                                .setFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        Conv1d.builder()
                                .setKernelShape(new Shape(3))
                                .setFilters(numberOfFilters)
                                .build())
                .add(Activation::relu)
                .add(
                        Conv1d.builder()
                                .setKernelShape(new Shape(3))
                                .setFilters(numberOfFilters)
                                .build())
                .add(Activation::relu)
                .add(
                        Conv1d.builder()
                                .setKernelShape(new Shape(3))
                                .setFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(Pool.maxPool1dBlock(new Shape(3), new Shape(3), new Shape(0)))
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder().setUnits(fullyConnected).build())
                .add(Activation.reluBlock())
                .add(Dropout.builder().optRate(dropoutProbability).build())
                .add(Linear.builder().setUnits(fullyConnected).build())
                .add(Activation::relu)
                .add(Dropout.builder().optRate(dropoutProbability).build())
                .add(Linear.builder().setUnits(2).build());

        model = Model.newInstance(modelName);
        model.setBlock(block);
    }

    static MaliciousURLModel getInstance() {
        return INSTANCE;
    }

    /** Load model. Note to call defineModel before calling this method, during inference. */
    void loadModel() throws IOException, MalformedModelException {
        Path modelPath = Paths.get(modelDir);
        Path modelFile = modelPath.resolve(modelName + "-0001.params");
        if (Files.notExists(modelFile)) {
            Files.createDirectories(modelPath);
            logger.info("Downloading model file ...");
            String url =
                    "https://djl-ai.s3.amazonaws.com/resources/demo/malicious-url-model/maliciousURLCNNModel-0002.params";
            Files.copy(new URL(url).openStream(), modelFile);
            logger.info("Model download success.");
        }
        model.load(modelPath, modelName);
    }

    /**
     * Inference on loaded model. call loadModel before this. Call flow defineModel -> loadModel ->
     * inference Calls URL Translator for pre-process and post-process functionality
     */
    Classifications inference(String url) {
        URLTranslator urlTranslator = new URLTranslator();
        try (Predictor<String, Classifications> predictor = model.newPredictor(urlTranslator)) {
            return predictor.predict(url);
        } catch (TranslateException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }
}
