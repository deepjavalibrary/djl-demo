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

package ai.djl.examples.quickdraw;

import java.io.IOException;
import java.util.List;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;

final class DoodleModel {

    private DoodleModel() {
    }

    public static ZooModel<Image, Classifications> loadModel() throws ModelException, IOException {
        DoodleTranslator translator = new DoodleTranslator();
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelUrls("https://djl-ai.s3.amazonaws.com/resources/demo/pytorch/doodle_mobilenet.zip")
                        .optTranslator(translator)
                        .build();
        return ModelZoo.loadModel(criteria);
    }

    private static class DoodleTranslator implements Translator<Image, Classifications> {

        private List<String> synset;

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) {
            NDArray array = list.singletonOrThrow();
            array = array.softmax(0);
            return new Classifications(synset, array);
        }

        @Override
        public NDList processInput(TranslatorContext ctx, Image image) {
            NDArray array = image.toNDArray(ctx.getNDManager(), Image.Flag.GRAYSCALE);
            return new NDList(new ToTensor().transform(array));
        }

        @Override
        public void prepare(NDManager manager, Model model) throws IOException {
            if (synset == null) {
                synset = model.getArtifact("synset.txt", Utils::readLines);
            }
        }

        @Override
        public Batchifier getBatchifier() {
            return Batchifier.STACK;
        }
    }
}
