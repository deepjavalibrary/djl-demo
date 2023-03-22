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
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.util.Arrays;

public class TextEmbedding {

    private TextEmbedding() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        String text = "This is an example sentence";

        Criteria<String, float[]> criteria =
                Criteria.builder()
                        .setTypes(String.class, float[].class)
                        .optModelUrls(
                                "djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                        .optEngine("PyTorch")
                        .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, float[]> model = criteria.loadModel();
                Predictor<String, float[]> predictor = model.newPredictor()) {
            float[] res = predictor.predict(text);
            System.out.println("Embedding: " + Arrays.toString(res));
        }
    }
}
