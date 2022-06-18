/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.example;

import static java.util.stream.Collectors.toMap;

import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class URLTranslator implements Translator<String, Classifications> {

    private static final int FEATURE_LENGTH = 1014;

    private List<Character> alphabets;
    private Map<Character, Integer> alphabetsIndex;

    /**
     * URLTranslator, like the Dataset defines encoding, to pre-process incoming inference requests
     */
    URLTranslator() {
        String allChars = "abcdefghijklmnopqrstuvwxyz0123456789-,;.!?:'\"/\\|_@#$%^&*~`+ =<>()[]{}";
        // Create an empty List of character
        alphabets = allChars.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        alphabetsIndex =
                IntStream.range(0, alphabets.size()).boxed().collect(toMap(alphabets::get, i -> i));
    }

    /**
     * processInput encodes the input URL string to a 69,1014 NDArray, works like Training data
     * encoder.
     *
     * @param ctx context of the translator.
     * @param url The input url sent to the FilterProxy.
     * @return NDList of encoded NDArray
     */
    @Override
    public NDList processInput(TranslatorContext ctx, String url) {
        NDArray encoded = ctx.getNDManager().zeros(new Shape(alphabets.size(), FEATURE_LENGTH));
        char[] arrayText = url.toCharArray();
        for (int i = 0; i < url.length(); i++) {
            if (i > FEATURE_LENGTH) {
                break;
            }
            if (alphabetsIndex.containsKey(arrayText[i])) {
                encoded.set(new NDIndex(alphabetsIndex.get(arrayText[i]), i), 1);
            }
        }
        NDList ndList = new NDList();
        ndList.add(encoded);
        return ndList;
    }

    /**
     * Converts the Output NDArray (classification labels) to Classifications object for easy
     * formatting.
     *
     * @param ctx context of the translator.
     * @param list NDlist of prediction output
     * @return returns a Classifications objects
     */
    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) {
        NDArray array = list.get(0);
        NDArray pred = array.softmax(-1);
        List<String> labels = new ArrayList<>();
        labels.add("benign");
        labels.add("malicious");
        return new Classifications(labels, pred);
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}
