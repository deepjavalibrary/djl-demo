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

package ai.djl.examples.neuralmachinetranslation;

import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Locale;

import ai.djl.ModelException;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.ParameterStore;

final class NeuralModel {

    private static final int HIDDEN_SIZE = 256;
    private static final int EOS_TOKEN = 1;
    private static final int MAX_LENGTH = 50;

    private NeuralModel() {
    }

    public static ZooModel<NDList, NDList> loadModelEncoder() throws ModelException, IOException {
        String url =
                "https://resources.djl.ai/demo/pytorch/android/neural_machine_translation/optimized_encoder_150k.zip";

        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelUrls(url)
                        .optModelName("optimized_encoder_150k.ptl")
                        .optEngine("PyTorch")
                        .build();
        return criteria.loadModel();
    }

    public static ZooModel<NDList, NDList> loadModelDecoder() throws ModelException, IOException {
        String url =
                "https://resources.djl.ai/demo/pytorch/android/neural_machine_translation/optimized_decoder_150k.zip";

        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelUrls(url)
                        .optModelName("optimized_decoder_150k.ptl")
                        .optEngine("PyTorch")
                        .build();
        return criteria.loadModel();
    }

    public static NDList predictEncoder(String text, ZooModel<NDList, NDList> model,
                                        LinkedTreeMap<String, Long> wrd2idx, NDManager manager)
            throws ModelException, IOException {
        // maps french input to id's from french file
        String[] french = text.split(" ");
        long[] inputs = new long[french.length];
        for (int i = 0; i < french.length; i++) {
            String word = french[i].toLowerCase(Locale.FRENCH);
            Long id = wrd2idx.get(word);
            if (id == null) {
                throw new ModelException("Word \"" + word + "\" not found.");
            }
            inputs[i] = id;
        }

        // for forwarding the model
        Shape inputShape = new Shape(1);
        Shape hiddenShape = new Shape(1, 1, 256);
        FloatBuffer fb = FloatBuffer.allocate(256);
        NDArray hiddenTensor = manager.create(fb, hiddenShape);
        long[] outputsShape = {MAX_LENGTH, HIDDEN_SIZE};
        FloatBuffer outputTensorBuffer = FloatBuffer.allocate(MAX_LENGTH * HIDDEN_SIZE);

        // for using the model
        Block block = model.getBlock();
        ParameterStore ps = new ParameterStore();

        // loops through forwarding of each word
        for (long input : inputs) {
            NDArray inputTensor = manager.create(new long[]{input}, inputShape);
            NDList inputTensorList = new NDList(inputTensor, hiddenTensor);
            NDList outputs = block.forward(ps, inputTensorList, false);
            NDArray outputTensor = outputs.get(0);
            outputTensorBuffer.put(outputTensor.toFloatArray());
            hiddenTensor = outputs.get(1);
        }
        outputTensorBuffer.rewind();
        NDArray outputsTensor = manager.create(outputTensorBuffer, new Shape(outputsShape));

        return new NDList(outputsTensor, hiddenTensor);
    }

    public static String predictDecoder(NDList toDecode, ZooModel<NDList, NDList> model,
                                        LinkedTreeMap<String, String> idx2wrd, NDManager manager)
            throws ModelException, IOException {
        StringBuilder english = new StringBuilder();

        // for forwarding the model
        Shape decoderInputShape = new Shape(1, 1);
        NDArray inputTensor = manager.create(new long[]{0}, decoderInputShape);
        ArrayList<Integer> result = new ArrayList<>(MAX_LENGTH);
        NDArray outputsTensor = toDecode.get(0);
        NDArray hiddenTensor = toDecode.get(1);

        // for using the model
        Block block = model.getBlock();
        ParameterStore ps = new ParameterStore();

        // loops through forwarding of each word
        for (int i = 0; i < MAX_LENGTH; i++) {
            NDList inputTensorList = new NDList(inputTensor, hiddenTensor, outputsTensor);
            NDList outputs = block.forward(ps, inputTensorList, false);
            NDArray outputTensor = outputs.get(0);
            hiddenTensor = outputs.get(1);
            float[] buf = outputTensor.toFloatArray();
            int topIdx = 0;
            double topVal = -Double.MAX_VALUE;
            for (int j = 0; j < buf.length; j++) {
                if (buf[j] > topVal) {
                    topVal = buf[j];
                    topIdx = j;
                }
            }

            if (topIdx == EOS_TOKEN) {
                break;
            }

            result.add(topIdx);
            inputTensor = manager.create(new long[]{topIdx}, decoderInputShape);
        }

        // map english words and create output string
        for (Integer word : result) {
            english.append(' ').append(idx2wrd.get(word.toString()));
        }
        return english.toString();
    }
}
