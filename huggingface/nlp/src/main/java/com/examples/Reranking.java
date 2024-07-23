/*
 * Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.util.StringPair;

import java.io.IOException;

public class Reranking {

    private Reranking() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        StringPair text =
                new StringPair(
                        "what is panda?",
                        "The giant panda (Ailuropoda melanoleuca), sometimes called a panda bear or"
                                + " simply panda, is a bear species endemic to China.");

        Criteria<StringPair, float[]> criteria =
                Criteria.builder()
                        .setTypes(StringPair.class, float[].class)
                        .optModelUrls("djl://ai.djl.huggingface.pytorch/BAAI/bge-reranker-v2-m3")
                        .optEngine("PyTorch")
                        .optArgument("reranking", "true")
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<StringPair, float[]> model = criteria.loadModel();
                Predictor<StringPair, float[]> predictor = model.newPredictor()) {
            float[] res = predictor.predict(text);
            System.out.println(res[0]);
        }
    }
}
