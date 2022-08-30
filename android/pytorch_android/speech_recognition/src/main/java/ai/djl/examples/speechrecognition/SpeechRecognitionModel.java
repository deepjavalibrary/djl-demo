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

package ai.djl.examples.speechrecognition;

import java.io.IOException;

import ai.djl.ModelException;
import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.translator.SpeechRecognitionTranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;

final class SpeechRecognitionModel {

    private SpeechRecognitionModel() {
    }

    public static ZooModel<Audio, String> loadModel() throws ModelException, IOException {
        String url = "https://resources.djl.ai/test-models/pytorch/wav2vec2.zip";
        Criteria<Audio, String> criteria =
                Criteria.builder()
                        .setTypes(Audio.class, String.class)
                        .optModelUrls(url)
                        .optTranslatorFactory(new SpeechRecognitionTranslatorFactory())
                        .optModelName("wav2vec2.ptl")
                        .optEngine("PyTorch")
                        .build();
        return criteria.loadModel();
    }
}
