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

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.util.BufferedImageUtils;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PneumoniaDetection {

    private static final Logger logger = LoggerFactory.getLogger(PneumoniaDetection.class);

    public static void main(String[] args)
            throws IOException, MalformedModelException, TranslateException,
                    ModelNotFoundException {
        String imagePath;
        if (args.length == 0) {
            imagePath =
                    "https://storage.googleapis.com/kagglesdsdata/datasets%2F17810%2F23812%2Fchest_xray%2Ftest%2FPNEUMONIA%2Fperson117_bacteria_553.jpeg?GoogleAccessId=gcp-kaggle-com@kaggle-161607.iam.gserviceaccount.com&Expires=1591659997&Signature=Wudos2zKg6DpLFfFzuIOHGE06%2BeF1atABjZjMi7Q%2Flx%2FLAy%2BEOuCkYJA0vqc6veame4r9FQMdomNWg0UIrd1A9gHSbL2%2FIXhfZMWkCavhfRtdaXsXKe1aldp1%2FracDceRu7vf4QX1ibsYVly8mbaLfjJEbdiuMsXsY3rk9CnTCbqSHDB6VcK75MexyEMMsWiHmyqjnDSiUyJ%2BPaFwZFfuNu%2B5dB%2FZoCwp%2BMmXEuLZJBX0T%2F8EU8VdKegkDfz3hRyZC31BawQFsDhUOdSdIEjvlECqJh4zA5A%2B2Gcqz9HkFFDyKzZqsefboR9tMzgsKc1QpmF55j%2BiM7Wv%2B0vHCJVcw%3D%3D";
            logger.info("Input image not specified, using image:\n\t{}", imagePath);
        } else {
            imagePath = args[0];
        }
        BufferedImage image;
        if (imagePath.startsWith("http")) {
            image = BufferedImageUtils.fromUrl(new URL(imagePath));
        } else {
            image = BufferedImageUtils.fromFile(Paths.get(imagePath));
        }

        Criteria<BufferedImage, Classifications> criteria =
                Criteria.builder()
                        .setTypes(BufferedImage.class, Classifications.class)
                        .optTranslator(new MyTranslator())
                        .build();

        try (ZooModel<BufferedImage, Classifications> model = ModelZoo.loadModel(criteria)) {
            try (Predictor<BufferedImage, Classifications> predictor = model.newPredictor()) {
                Classifications result = predictor.predict(image);
                logger.info("Diagnose:" + result.toString());
            }
        }
    }

    private static final class MyTranslator implements Translator<BufferedImage, Classifications> {

        private static final List<String> CLASSES = Arrays.asList("Normal", "Pneumonia");

        @Override
        public NDList processInput(TranslatorContext ctx, BufferedImage input) {
            NDArray array =
                    BufferedImageUtils.toNDArray(
                            ctx.getNDManager(), input, NDImageUtils.Flag.COLOR);
            array = NDImageUtils.resize(array, 224).div(255.0f);
            return new NDList(array);
        }

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) {
            NDArray probabilities = list.singletonOrThrow();
            return new Classifications(CLASSES, probabilities);
        }
    }
}
