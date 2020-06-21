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
package com.example;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ImageClassification {

    private static final String MODEL_URL = "https://djl-tensorflow-javacpp.s3.amazonaws.com/tensorflow-models/chest_x_ray/saved_model.zip";
    private static final String IMAGE_URL = "https://djl-ai.s3.amazonaws.com/resources/images/chest_xray.jpg";
    private static final List<String> CLASSES = Arrays.asList("Normal", "Pneumonia");

    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        // java default ImageIO doesn't work with GraalVM
        ImageFactory.setImageFactory(new GraalvmImageFactory());

        Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                .addTransform(new CenterCrop())
                .addTransform(new Resize(224, 224))
                .addTransform(new ToTensor())
                .optSynset(CLASSES)
                .build();
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelUrls(MODEL_URL)
                        .optTranslator(translator)
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
             Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Image image = ImageFactory.getInstance().fromUrl(IMAGE_URL);

            Classifications result = predictor.predict(image);
            System.out.println(result.toString());
        }
    }
}
