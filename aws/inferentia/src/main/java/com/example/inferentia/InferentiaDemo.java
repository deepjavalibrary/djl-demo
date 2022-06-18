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
package com.example.inferentia;

import ai.djl.ModelException;
import ai.djl.engine.Engine;
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
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

import java.io.IOException;
import java.nio.file.Paths;

public class InferentiaDemo {

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        String version = Engine.getEngine("PyTorch").getVersion();
        System.out.println("Running inference with PyTorch: " + version);

        // You need manually load libneuron_op.so prior to 0.12.0, uncomment the following code
        // if use 0.11.0 release. And libneuron_op.so must be loaded after PyTorch engine is loaded.
        /*
        String extraPath = System.getenv("PYTORCH_EXTRA_LIBRARY_PATH");
        if (extraPath != null) {
            System.load(extraPath);
        } else {
            System.loadLibrary("neuron_op");
        }
        */

        String url = "https://resources.djl.ai/images/kitten.jpg";
        Image img = ImageFactory.getInstance().fromUrl(url);
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelPath(Paths.get("models/inferentia/resnet50"))
                        .optTranslator(getTranslator())
                        .build();

        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
                Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Classifications result = predictor.predict(img);
            System.out.println(result);
        }
    }

    private static Translator<Image, Classifications> getTranslator() {
        return ImageClassificationTranslator.builder()
                .addTransform(new CenterCrop())
                .addTransform(new Resize(224, 224))
                .addTransform(new ToTensor())
                .optSynsetUrl(InferentiaDemo.class.getResource("/synset.txt").toString())
                .optApplySoftmax(true)
                .build();
    }
}
