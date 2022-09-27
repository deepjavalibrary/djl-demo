/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package ai.djl.examples.objectdetection;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.djl.ModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;

final class ObjectDetectionModel {

    private ObjectDetectionModel() {
    }

    public static ZooModel<Image, DetectedObjects> loadModel(Path modelPath) throws ModelException, IOException {

        Map<String, String> arguments = new ConcurrentHashMap<>();
        arguments.put("toTensor", "true");
        arguments.put("normalize", "true");
        YoloV5Translator translator =
                YoloV5Translator.builder(arguments).build();

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelPath(modelPath)
                        .optTranslator(translator)
                        .optEngine("OnnxRuntime")
                        .build();
        return criteria.loadModel();
    }
}
