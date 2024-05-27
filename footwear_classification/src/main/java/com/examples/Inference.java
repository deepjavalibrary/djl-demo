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

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.training.util.DownloadUtils;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Uses the model to generate a prediction called an inference */
public class Inference {

    public static void main(String[] args) throws ModelException, TranslateException, IOException {
        // the location where the model is saved
        Path modelDir = Paths.get("models");

        // the path of image to classify
        Path imageFilePath;
        if (args.length == 0) {
            imageFilePath = Paths.get("ut-zap50k-images-square/Sandals/Heel/Annie/7350693.3.jpg");
        } else {
            imageFilePath = Paths.get(args[0]);
        }

        if (Files.notExists(imageFilePath)) {
            System.out.println("Please specify and image file or download the dataset.");
            return;
        }

        downloadModelIfNeeded(modelDir);

        // Load the image file from the path
        Image img = ImageFactory.getInstance().fromFile(imageFilePath);

        try (Model model = Models.getModel()) { // empty model instance
            // load the model
            model.load(modelDir, Models.MODEL_NAME);

            // define a translator for pre and post processing
            // out of the box this translator converts images to ResNet friendly ResNet 18 shape
            Translator<Image, Classifications> translator =
                    ImageClassificationTranslator.builder()
                            .addTransform(new Resize(Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT))
                            .addTransform(new ToTensor())
                            .optApplySoftmax(true)
                            .build();

            // run the inference using a Predictor
            try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
                // holds the probability score per label
                Classifications predictResult = predictor.predict(img);
                System.out.println(predictResult);
            }
        }
    }

    private static void downloadModelIfNeeded(Path modelDir) throws IOException {
        Path modelFle = modelDir.resolve("shoeclassifier-0002.params");
        String url =
                "https://resources.djl.ai/demo/footwear_classification/shoeclassifier-0002.params";
        DownloadUtils.download(new URL(url), modelFle, null);

        Path synsetFle = modelDir.resolve("synset.txt");
        url = "https://resources.djl.ai/demo/footwear_classification/synset.txt";
        DownloadUtils.download(new URL(url), synsetFle, null);
    }
}
