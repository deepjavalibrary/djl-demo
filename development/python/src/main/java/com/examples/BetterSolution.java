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
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public final class BetterSolution {

    private BetterSolution() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        PythonTranslator translator = new PythonTranslator();
        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .setTypes(String.class, Classifications.class)
                        .optModelUrls("djl://ai.djl.pytorch/resnet")
                        .optTranslator(translator)
                        .build();
        String url = "https://resources.djl.ai/images/kitten.jpg";
        try (ZooModel<String, Classifications> model = criteria.loadModel();
                Predictor<String, Classifications> predictor = model.newPredictor()) {
            Classifications ret = predictor.predict(url);
            System.out.println(ret);
        }

        // unload python model
        translator.close();
    }
}
