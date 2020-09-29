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
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Path("/")
public class ImageClassification {

    private static final String IMAGE_URL = "https://djl-ai.s3.amazonaws.com/resources/images/kitten_small.jpg";

    private static final float[] MEAN = {103.939f, 116.779f, 123.68f};
    private static final float[] STD = {1f, 1f, 1f};

    @Path("/detect")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String detect() throws TranslateException, IOException, ModelException, ImageReadException {
        URL url = new URL(IMAGE_URL);
        Image image;
        try (InputStream is = url.openStream()) {
            image = ImageFactory.getInstance().fromImage(Imaging.getBufferedImage(is));
        }

        Criteria<Image, Classifications> criteria;
        if ("TensorFlow".equals(Engine.getInstance().getEngineName())) {
            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                    .addTransform(new Resize(224))
                    .addTransform(new Normalize(MEAN, STD))
                    .build();
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optArtifactId("resnet")
                            .optTranslator(translator)
                            .optProgress(new ProgressBar())
                            .build();
        } else {
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optArtifactId("resnet")
                            .optProgress(new ProgressBar())
                            .build();
        }

        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
             Predictor<Image, Classifications> predictor = model.newPredictor()) {

            Classifications result = predictor.predict(image);
            return result.toString() + "\n";
        }
    }
}
