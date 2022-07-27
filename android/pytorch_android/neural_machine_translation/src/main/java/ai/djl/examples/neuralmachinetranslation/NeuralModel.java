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
import java.nio.LongBuffer;
import java.util.ArrayList;

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

    private NeuralModel() {}

    public static ZooModel<NDList, NDList> loadModelEncoder() throws ModelException, IOException {
        String url =
                "https://djl-misc.s3.amazonaws.com/tmp/neural_machine_translation/ai/djl/pytorch/0.0.1/optimized_encoder_150k.ptl.zip";

        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelUrls(url)
                        .optEngine("PyTorch")
                        .build();
        return criteria.loadModel();
    }

    public static ZooModel<NDList, NDList> loadModelDecoder() throws ModelException, IOException {
        String url =
                "https://djl-misc.s3.amazonaws.com/tmp/neural_machine_translation/ai/djl/pytorch/0.0.1/optimized_decoder_150k.ptl.zip";

        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelUrls(url)
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
        String word = "";
        try {
            for (int i = 0; i < french.length; i++) {
                word = french[i].toLowerCase();
                inputs[i] = wrd2idx.get(word);
            }
        } catch (Exception e) {
            throw new ModelException("Word \"" + word + "\" not found.");
        }

        // for forwarding the model
        Shape inputShape = new Shape(1);
        Shape hiddenShape = new Shape(1, 1, 256);
        FloatBuffer fb = FloatBuffer.allocate(1 * 1 * 256);
        NDArray hiddenTensor = manager.create(fb, hiddenShape);
        final long[] outputsShape = {MAX_LENGTH, HIDDEN_SIZE};
        final FloatBuffer outputTensorBuffer = FloatBuffer.allocate(MAX_LENGTH * HIDDEN_SIZE);

        // for using the model
        Block block = model.getBlock();
        ParameterStore ps = new ParameterStore();

        // loops through forwarding of each word
        for (long input : inputs) {
            LongBuffer lb = LongBuffer.allocate(1);
            lb.put(input);
            lb.rewind();
            NDArray inputTensor = manager.create(lb, inputShape);
            NDList inputTensorList = new NDList(inputTensor, hiddenTensor);
            NDList outputTuple = block.forward(ps, inputTensorList, false);
            final NDArray outputTensor = outputTuple.get(0);
            outputTensorBuffer.put(outputTensor.toFloatArray());
            hiddenTensor = outputTuple.get(1);
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
        LongBuffer inputTensorBuffer = LongBuffer.allocate(1);
        inputTensorBuffer.put(0);
        inputTensorBuffer.rewind();
        NDArray inputTensor = manager.create(inputTensorBuffer, decoderInputShape);
        ArrayList<Integer> result = new ArrayList<>(MAX_LENGTH);
        NDArray outputsTensor = toDecode.get(0);
        NDArray hiddenTensor = toDecode.get(1);

        // for using the model
        Block block = model.getBlock();
        ParameterStore ps = new ParameterStore();

        // loops through forwarding of each word
        for (int i = 0; i < MAX_LENGTH; i++) {
            NDList inputTensorList = new NDList(inputTensor, hiddenTensor, outputsTensor);
            NDList outputsTuple = block.forward(ps, inputTensorList, false);
            final NDArray outputTensor = outputsTuple.get(0);
            hiddenTensor = outputsTuple.get(1);
            float[] outputs = outputTensor.toFloatArray();
            int topIdx = 0;
            double topVal = -Double.MAX_VALUE;
            for (int j = 0; j < outputs.length; j++) {
                if (outputs[j] > topVal) {
                    topVal = outputs[j];
                    topIdx = j;
                }
            }

            if (topIdx == EOS_TOKEN) {
                break;
            }

            result.add(topIdx);
            inputTensorBuffer = LongBuffer.allocate(1);
            inputTensorBuffer.put(topIdx);
            inputTensorBuffer.rewind();
            inputTensor = manager.create(inputTensorBuffer, decoderInputShape);
        }

        // map english words and create output string
        for (Integer word : result) {
            english.append(" " + idx2wrd.get(word.toString()));
        }
        return english.toString();
    }
}
