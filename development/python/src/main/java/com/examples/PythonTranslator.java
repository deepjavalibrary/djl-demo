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
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.JsonUtils;
import ai.djl.util.Utils;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PythonTranslator implements NoBatchifyTranslator<String, Classifications> {

    private ZooModel<Input, Output> model;
    private Predictor<Input, Output> predictor;

    @Override
    public void prepare(TranslatorContext ctx) throws ModelException, IOException {
        if (predictor == null) {
            Criteria<Input, Output> criteria =
                    Criteria.builder()
                            .setTypes(Input.class, Output.class)
                            .optModelPath(Paths.get("src/test/resources/resnet18"))
                            .optEngine("Python")
                            .build();
            model = criteria.loadModel();
            predictor = model.newPredictor();
        }
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String url)
            throws IOException, TranslateException {
        Input input = new Input();
        try (InputStream is = new URL(url).openStream()) {
            input.add("data", Utils.toByteArray(is));
        }
        input.addProperty("Content-Type", "image/jpeg");
        // calling preprocess() function in model.py
        input.addProperty("handler", "preprocess");
        Output output = predictor.predict(input);
        if (output.getCode() != 200) {
            throw new TranslateException("Python preprocess() failed: " + output.getMessage());
        }

        /* TODO: use the following code in 0.22.0
        return output.getDataAsNDList(ctx.getNDManager());
        */

        // Workaround a bug in 0.21.0
        NDArray array = output.getDataAsNDList(ctx.getNDManager()).get(0);
        array = ctx.getNDManager().create(array.toFloatArray(), new Shape(1, 3, 224, 224));
        return new NDList(array);
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list)
            throws TranslateException {
        Input input = new Input();
        input.add("data", list);
        // calling postprocess() function in processing.py
        input.addProperty("handler", "postprocess");
        Output output = predictor.predict(input);
        if (output.getCode() != 200) {
            throw new TranslateException("Python postprocess() failed: " + output.getMessage());
        }

        String json = output.getData().getAsString();
        Type type = new TypeToken<Map<String, Double>>() {}.getType();
        Map<String, Double> map = JsonUtils.GSON.fromJson(json, type);
        List<String> keys = new ArrayList<>(map.keySet());
        List<Double> values = new ArrayList<>(map.values());
        return new Classifications(keys, values);
    }

    public void close() {
        if (predictor != null) {
            predictor.close();
            model.close();
            predictor = null;
            model = null;
        }
    }
}
