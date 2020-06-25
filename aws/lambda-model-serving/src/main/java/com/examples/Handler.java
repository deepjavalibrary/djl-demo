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
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.util.Utils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Handler implements RequestStreamHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        // DJL saves model and native libraries in cache folder.
        // In AWS-Lambda only /tmp folder is writable.
        System.setProperty("DJL_CACHE_DIR", "/tmp/djl_cache");
    }

    @Override
    public void handleRequest(InputStream is, OutputStream os, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        String input = Utils.toString(is);
        try {
            Request request = GSON.fromJson(input, Request.class);
            String url = request.getInputImageUrl();
            String artifactId = request.getArtifactId();
            Map<String, String> filters = request.getFilters();
            Criteria<Image, Classifications> criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optArtifactId(artifactId)
                            .optFilters(filters)
                            .build();
            try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
                    Predictor<Image, Classifications> predictor = model.newPredictor()) {
                Image image = ImageFactory.getInstance().fromUrl(url);
                List<Classifications.Classification> result = predictor.predict(image).topK(5);
                os.write(GSON.toJson(result).getBytes(StandardCharsets.UTF_8));
            }
        } catch (RuntimeException | ModelException | TranslateException e) {
            logger.log("Failed handle input: " + input);
            logger.log(e.toString());
            String msg = "{\"status\": \"invoke failed: " + e.toString() + "\"}";
            os.write(msg.getBytes(StandardCharsets.UTF_8));
        }
    }
}
