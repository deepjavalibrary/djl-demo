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

package ai.djl.examples.semanticsegmentation;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.translator.SemanticSegmentationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;

final class SemanticModel {

    private SemanticModel() {
    }

    public static ZooModel<Image, Image> loadModel() throws ModelException, IOException {
        String url =
                "https://mlrepo.djl.ai/model/cv/semantic_segmentation/ai/djl/pytorch/deeplabv3/0.0.1/deeplabv3.zip";
        Map<String, String> arguments = new ConcurrentHashMap<>();
        arguments.put("toTensor", "true");
        arguments.put("normalize", "true");
        SemanticSegmentationTranslator translator =
                SemanticSegmentationTranslator.builder(arguments).build();

        Criteria<Image, Image> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Image.class)
                        .optModelUrls(url)
                        .optTranslator(translator)
                        .optEngine("PyTorch")
                        .optProgress(new ProgressBar())
                        .build();
        return ModelZoo.loadModel(criteria);
    }
}
