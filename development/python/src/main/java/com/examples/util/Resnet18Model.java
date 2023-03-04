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
package com.examples.util;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopServingTranslatorFactory;

import java.io.IOException;

public class Resnet18Model {

    private ZooModel<Input, Output> resnet18;

    public Predictor<Input, Output> getPredictor() throws ModelException, IOException {
        if (resnet18 == null) {
            Criteria<Input, Output> criteria =
                    Criteria.builder()
                            .setTypes(Input.class, Output.class)
                            .optModelUrls("djl://ai.djl.pytorch/resnet")
                            .optTranslatorFactory(new NoopServingTranslatorFactory())
                            .build();
            resnet18 = criteria.loadModel();
        }
        return resnet18.newPredictor();
    }

    public void close() {
        if (resnet18 != null) {
            resnet18.close();
        }
    }
}
