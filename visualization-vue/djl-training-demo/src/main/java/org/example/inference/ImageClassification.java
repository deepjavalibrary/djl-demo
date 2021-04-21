/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package org.example.inference;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.training.util.DownloadUtils;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An example of inference using an image classification model.
 *
 * <p>See this <a
 * href="https://github.com/deepjavalibrary/djl/blob/master/examples/docs/image_classification.md">doc</a>
 * for information about this example.
 */
public final class ImageClassification {

    private ImageClassification() {
    }

    public static Classifications predict(Image img) throws IOException, ModelException, TranslateException {
        String modelName = "mlp";
        try (Model model = Model.newInstance(modelName)) {
            model.setBlock(new Mlp(28 * 28, 10, new int[]{128, 64}));

            // Assume you have run TrainMnist.java example, and saved model in build/model folder.
            Path modelDir = Paths.get("model/mnist");
            Files.createDirectories(modelDir);
            Path mlp = modelDir.resolve("mlp-0000.params");
            if (!Files.exists(mlp)) {
                String url = "https://mlrepo.djl.ai/model/cv/image_classification/ai/djl/zoo/mlp/0.0.3/mlp-0000.params.gz";
                DownloadUtils.download(url, "model/mnist/mlp-0000.params");
            }
            model.load(modelDir);

            List<String> classes =
                    IntStream.range(0, 10).mapToObj(String::valueOf).collect(Collectors.toList());
            Translator<Image, Classifications> translator =
                    ImageClassificationTranslator.builder()
                            .addTransform(new ToTensor())
                            .optSynset(classes)
                            .build();

            try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
                return predictor.predict(img);
            }
        }
    }
}
