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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.djl.ModelException;
import ai.djl.modality.audio.translator.SpeechRecognitionTranslator;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;

final class SpeechRecognitionModel {

    private SpeechRecognitionModel() {
    }

    public static ZooModel<NDList, NDList> loadModel() throws ModelException, IOException {
        String url =
                "https://djl-misc.s3.amazonaws.com/tmp/speech_recognition/ai/djl/pytorch/wav2vec2/0.0.1/wav2vec2.pt.zip";
        Map<String, String> arguments = new ConcurrentHashMap<>();
        SpeechRecognitionTranslator translator =
                SpeechRecognitionTranslator.builder(arguments).build();

        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelUrls(url)
                        .optTranslator(translator)
//                        .optModelName("wav2vec2.pt")
                        .optEngine("PyTorch")
                        .build();
        return criteria.loadModel();
    }
}
