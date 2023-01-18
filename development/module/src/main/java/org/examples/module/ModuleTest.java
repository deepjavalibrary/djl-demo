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
package org.examples.module;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Module test for DJL. */
@SuppressWarnings("MissingJavadocMethod")
public final class ModuleTest {

    private static final Logger logger = LoggerFactory.getLogger(ModuleTest.class);

    private ModuleTest() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        logger.info("----------Test inference----------");
        String url = "https://resources.djl.ai/images/dog_bike_car.jpg";
        Image img = ImageFactory.getInstance().fromUrl(url);
        String backbone = "resnet50";
        Map<String, String> options = null;
        if ("TensorFlow".equals(Engine.getInstance().getEngineName())) {
            backbone = "mobilenet_v2";
            options = new ConcurrentHashMap<>();
            options.put("Tags", "");
        }

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optFilter("backbone", backbone)
                        .optOptions(options)
                        .build();

        try (ZooModel<Image, DetectedObjects> model = criteria.loadModel();
                Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            DetectedObjects detection = predictor.predict(img);
            logger.info("{}", detection);
        }
    }
}
