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
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.net.URL;

public final class FatJar {

    private FatJar() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        Criteria<URL, Classifications> criteria =
                Criteria.builder()
                        .setTypes(URL.class, Classifications.class)
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch")
                        .optModelUrls("djl://ai.djl.pytorch/resnet/0.0.1/traced_resnet18")
                        .build();

        URL url = new URL("https://resources.djl.ai/images/kitten.jpg");
        try (ZooModel<URL, Classifications> model = criteria.loadModel();
                Predictor<URL, Classifications> predictor = model.newPredictor()) {
            Classifications classifications = predictor.predict(url);
            System.out.println(classifications);
        }
    }
}
