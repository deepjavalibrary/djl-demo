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

    @Path("/detect")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String detect() throws TranslateException, IOException, ModelException, ImageReadException {
        URL url = new URL(IMAGE_URL);
        Image image;
        try (InputStream is = url.openStream()) {
            image = ImageFactory.getInstance().fromImage(Imaging.getBufferedImage(is));
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
            return result.toString() + "\n";
        }
    }
}
