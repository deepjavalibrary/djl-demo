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
package com.example.djlInferenceBeanStalk;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class Handler {
    Logger logger = Logger.getLogger(Handler.class.getName());

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private class DJLModel {
        ZooModel<Image, Classifications> model;
        Predictor<Image, Classifications> predictor;

        public DJLModel() throws IOException, ModelException {
            // Uses the Criteria class to define and load in the model
            Criteria<Image, Classifications> criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optArtifactId("ai.djl.localmodelzoo:doodle_mobilenet")
                            .optTranslator(new DoodleTranslator())
                            // Sets the url for where to fetch the model from
                            .optModelUrls("https://alpha-djl-demos.s3.amazonaws.com/model/quickdraw/doodle_mobilenet.zip")
                            .build();
            this.model = ModelZoo.loadModel(criteria);

            this.predictor = model.newPredictor();
        }

        public ZooModel<Image,Classifications> getModel() {
            return model;
        }

        public Predictor<Image, Classifications> getPredictor() {
            return predictor;
        }
    }

    DJLModel djlModel;

    /**
     * This method is the Rest endpoint where the user can post their images
     * to run inference against a model of their choice using DJL!
     * @param inputRequest The request body containing the image in String format
     * @return Returns the top 5 probable items from the model output
     * @throws IOException
     */
    @PostMapping(value = "/inference")
    public byte[] handleRequest(@RequestBody Request inputRequest) throws IOException {
        // Grabs byte array image from the input
        byte[] imgBytes = inputRequest.getImageData();

        try {
            // Checks if djlModel has been initialized, otherwise initialize it
            // This is to avoid reloading it on each new request.
            if (djlModel == null)
                djlModel = new DJLModel();

            // Transforms the byte array into an image
            Image img;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes)) {
                ImageFactory factory = ImageFactory.getInstance();
                img = factory.fromInputStream(bis);
            }

            // Does inference on the image and returns the results
            List<Classifications.Classification> result = djlModel.getPredictor().predict(img).topK(5);
            return GSON.toJson(result).getBytes(StandardCharsets.UTF_8);

        } catch (RuntimeException | ModelException | TranslateException e) {
            logger.info("Failed handle input: " + inputRequest);
            logger.info(e.toString());
            String msg = "{\"status\": \"invoke failed: " + e.toString() + "\"}";
            return msg.getBytes();
        }
    }

    static class DoodleTranslator implements Translator<Image, Classifications> {

        private List<String> synset;

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) throws Exception {
            if (synset == null) {
                synset = ctx.getModel().getArtifact("synset.txt", Utils::readLines);
            }
            NDArray array = list.singletonOrThrow();
            array = array.softmax(0);
            return new Classifications(synset, array);
        }

        @Override
        public NDList processInput(TranslatorContext ctx, Image input) throws Exception {
            NDArray img = input.toNDArray(ctx.getNDManager(), Image.Flag.GRAYSCALE);
            img = new ToTensor().transform(img);
            return new NDList(img);
        }
    }
}
