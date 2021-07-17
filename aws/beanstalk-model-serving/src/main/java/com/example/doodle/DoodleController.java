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
package com.example.doodle;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DoodleController {

    private static final Logger logger = LoggerFactory.getLogger(DoodleController.class);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MODEL_URL =
            "https://djl-ai.s3.amazonaws.com/resources/demo/pytorch/doodle_mobilenet.zip";

    @Autowired ZooModel<Image, Classifications> model;

    /**
     * This method is the Rest endpoint where the user can post their images to run inference
     * against a model of their choice using DJL!
     *
     * @param input the request body containing the image
     * @return returns the top 3 probable items from the model output
     * @throws IOException if failed read HTTP request
     */
    @PostMapping(value = "/doodle")
    public String handleRequest(InputStream input) throws IOException {
        Image img = ImageFactory.getInstance().fromInputStream(input);
        try (Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Classifications classifications = predictor.predict(img);
            return GSON.toJson(classifications.topK(3)) + System.lineSeparator();
        } catch (RuntimeException | TranslateException e) {
            logger.error("", e);
            Map<String, String> error = new ConcurrentHashMap<>();
            error.put("status", "Invoke failed: " + e);
            return GSON.toJson(error) + System.lineSeparator();
        }
    }

    @Bean
    public ZooModel<Image, Classifications> model() throws ModelException, IOException {
        Translator<Image, Classifications> translator =
                ImageClassificationTranslator.builder()
                        .optFlag(Image.Flag.GRAYSCALE)
                        .setPipeline(new Pipeline(new ToTensor()))
                        .optApplySoftmax(true)
                        .build();
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelUrls(MODEL_URL)
                        .optTranslator(translator)
                        .build();
        return criteria.loadModel();
    }
}
