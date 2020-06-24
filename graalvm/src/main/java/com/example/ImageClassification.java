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
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class ImageClassification {

    private static final String IMAGE_URL = "https://djl-ai.s3.amazonaws.com/resources/images/kitten_small.jpg";

    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        // java default ImageIO doesn't work with GraalVM
        ImageFactory.setImageFactory(new GraalvmImageFactory());

        Image image;
        if (args.length == 0) {
            image = ImageFactory.getInstance().fromUrl(IMAGE_URL);
        } else {
            image = ImageFactory.getInstance().fromUrl(args[0]);
        }

        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optArtifactId("resnet")
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
             Predictor<Image, Classifications> predictor = model.newPredictor()) {

            Classifications result = predictor.predict(image);
            System.out.println(result.toString());
        }
    }
}
