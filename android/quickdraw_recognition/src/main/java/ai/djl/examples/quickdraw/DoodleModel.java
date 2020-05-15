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

import android.graphics.Bitmap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

final class DoodleModel {

    private Predictor<Bitmap, Classifications> predictor;

    DoodleModel(Path directory) throws IOException, MalformedModelException {
        Model model = Model.newInstance();
        model.load(directory, "doodle_mobilenet");
        predictor = model.newPredictor(new DoodleTranslator(directory));
    }

    Classifications predict(Bitmap bitmap) {
        try {
            return predictor.predict(bitmap);
        } catch (TranslateException e) {
           throw new IllegalStateException("Failed translation", e);
        }
    }

    private static class DoodleTranslator implements Translator<Bitmap, Classifications> {

        private List<String> synset;

        private DoodleTranslator(Path directory) throws IOException {
            synset = Files.readAllLines(directory.resolve("synset.txt"));
        }

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) {
            NDArray array = list.singletonOrThrow();
            array = array.softmax(0);
            return new Classifications(synset, array);
        }

        @Override
        public NDList processInput(TranslatorContext ctx, Bitmap input) {
            Image image = ImageFactory.getInstance().fromImage(input);
            NDArray array = image.toNDArray(ctx.getNDManager(), Image.Flag.GRAYSCALE);
            return new NDList(new ToTensor().transform(array));
        }
    }
}
