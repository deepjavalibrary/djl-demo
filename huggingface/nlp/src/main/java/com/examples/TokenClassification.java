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
import ai.djl.huggingface.translator.TokenClassificationTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.translator.NamedEntity;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.util.JsonUtils;

import java.io.IOException;

public class TokenClassification {

    private TokenClassification() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        String text = "My name is Wolfgang and I live in Berlin.";

        Criteria<String, NamedEntity[]> criteria =
                Criteria.builder()
                        .setTypes(String.class, NamedEntity[].class)
                        .optModelUrls(
                                "djl://ai.djl.huggingface.pytorch/ml6team/bert-base-uncased-city-country-ner")
                        .optEngine("PyTorch")
                        .optTranslatorFactory(new TokenClassificationTranslatorFactory())
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, NamedEntity[]> model = criteria.loadModel();
                Predictor<String, NamedEntity[]> predictor = model.newPredictor()) {
            NamedEntity[] res = predictor.predict(text);
            System.out.println(JsonUtils.GSON_PRETTY.toJson(res));
        }
    }
}
