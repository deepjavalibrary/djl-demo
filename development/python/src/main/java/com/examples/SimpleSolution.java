/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.util.DownloadUtils;
import ai.djl.translate.TranslateException;

import com.examples.util.ProcessingModel;
import com.examples.util.Resnet18Model;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SimpleSolution {

    private SimpleSolution() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        URL url = new URL("https://resources.djl.ai/images/kitten.jpg");
        Path file = Paths.get("build/test/kitten.jpg");
        DownloadUtils.download(url, file, null);
        byte[] image = Files.readAllBytes(file);

        ProcessingModel processing = new ProcessingModel();
        Resnet18Model resnet18 = new Resnet18Model();
        try (Predictor<Input, Output> predictor = resnet18.getPredictor();
                Predictor<Input, Output> processingPredictor = processing.getPredictor()) {

            Input preProcessing = new Input();
            preProcessing.add("data", image);
            preProcessing.addProperty("Content-Type", "image/jpeg");
            // calling preprocess() function in model.py
            preProcessing.addProperty("handler", "preprocess");
            Output preprocessed = processingPredictor.predict(preProcessing);

            // Show preprocessed output NDList for demo purpose
            showPreprocessedData(preprocessed);

            // Pass preprocessed data to PyTorch resnet18 model
            Input tensor = new Input();
            tensor.add("data", preprocessed.getData());
            preProcessing.addProperty("Content-Type", "tensor/ndlist");
            Output output = predictor.predict(tensor);

            // Show resnet18 model output NDList for demo purpose
            showModelOutput(output);

            Input postProcessing = new Input();
            postProcessing.add("data", output.getData());
            // calling postprocess() function in processing.py
            postProcessing.addProperty("handler", "postprocess");
            Output ret = processingPredictor.predict(postProcessing);

            String json = ret.getData().getAsString();
            System.out.println(json);
        }

        // unload models
        processing.close();
        resnet18.close();
    }

    private static void showPreprocessedData(Output output) {
        if (output.getCode() != 200) {
            throw new IllegalStateException("Preprocessing failed.");
        }

        try (NDManager manager = NDManager.newBaseManager()) {
            NDList list = output.getDataAsNDList(manager);
            NDArray array = list.singletonOrThrow();
            System.out.println("Result of python image preprocessing: " + array);
        }
    }

    private static void showModelOutput(Output output) {
        if (output.getCode() != 200) {
            throw new IllegalStateException("Preprocessing failed.");
        }

        // Show preprocessed output NDList for demo purpose
        try (NDManager manager = NDManager.newBaseManager()) {
            NDList list = output.getDataAsNDList(manager);
            NDArray array = list.singletonOrThrow();
            System.out.println("Result of python image preprocessing: " + array);
        }
    }
}
