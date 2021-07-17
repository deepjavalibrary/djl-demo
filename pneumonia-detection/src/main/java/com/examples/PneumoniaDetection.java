/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PneumoniaDetection {

    private static final Logger logger = LoggerFactory.getLogger(PneumoniaDetection.class);

    private static final List<String> CLASSES = Arrays.asList("Normal", "Pneumonia");

    public static void main(String[] args) throws IOException, TranslateException, ModelException {
        String imagePath;
        if (args.length == 0) {
            imagePath = "https://djl-ai.s3.amazonaws.com/resources/images/chest_xray.jpg";
            logger.info("Input image not specified, using image:\n\t{}", imagePath);
        } else {
            imagePath = args[0];
        }
        Image image;
        if (imagePath.startsWith("http")) {
            image = ImageFactory.getInstance().fromUrl(imagePath);
        } else {
            image = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        }

        Translator<Image, Classifications> translator =
                ImageClassificationTranslator.builder()
                        .addTransform(a -> NDImageUtils.resize(a, 224).div(255.0f))
                        .optSynset(CLASSES)
                        .build();
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelName("saved_model")
                        .optTranslator(translator)
                        .build();

        try (ZooModel<Image, Classifications> model = criteria.loadModel();
                Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Classifications result = predictor.predict(image);
            logger.info("Diagnose: {}", result);
        }
    }
}
