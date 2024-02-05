/*
 * Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.examples;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.llama.engine.LlamaInput;
import ai.djl.llama.engine.LlamaTranslatorFactory;
import ai.djl.llama.jni.Token;
import ai.djl.llama.jni.TokenIterator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public class Chatbot {

    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        String modelId;
        String quantMethod;
        if (args.length > 0) {
            modelId = args[0];
            if (args.length > 1) {
                quantMethod = args[1];
            } else {
                quantMethod = "Q4_K_M";
            }
        } else {
            // modelId = "TheBloke/Mistral-7B-Instruct-v0.2-GGUF";
            // quantMethod = "Q4_K_M";
            modelId = "TinyLlama/TinyLlama-1.1B-Chat-v0.6";
            quantMethod = "Q4_0";
        }
        System.out.println("Using model: " + modelId);

        String url = "djl://ai.djl.huggingface.gguf/" + modelId + "/0.0.1/" + quantMethod;
        Criteria<LlamaInput, TokenIterator> criteria =
                Criteria.builder()
                        .setTypes(LlamaInput.class, TokenIterator.class)
                        .optModelUrls(url)
                        .optEngine("Llama")
                        .optOption("number_gpu_layers", "43")
                        .optTranslatorFactory(new LlamaTranslatorFactory())
                        .optProgress(new ProgressBar())
                        .build();

        String system =
                "This is demo for DJL Llama.cpp engine.\n\n"
                        + "Llama: Hello.  How may I help you today?";

        LlamaInput.Parameters param = new LlamaInput.Parameters();
        param.setTemperature(0.7f);
        param.setPenalizeNl(true);
        param.setMirostat(2);
        param.setAntiPrompt(new String[] {"User: "});

        LlamaInput in = new LlamaInput();
        in.setParameters(param);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try (ZooModel<LlamaInput, TokenIterator> model = criteria.loadModel();
                Predictor<LlamaInput, TokenIterator> predictor = model.newPredictor()) {
            System.out.print(system);
            StringBuilder prompt = new StringBuilder(system);
            Set<String> exitWords = Set.of("exit", "bye", "quit");
            while (true) {
                System.out.print("\nUser: ");
                String input = reader.readLine().trim();
                if (exitWords.contains(input.toLowerCase(Locale.ROOT))) {
                    break;
                }
                System.out.print("Llama: ");
                prompt.append("\nUser: ").append(input).append("\nLlama: ");
                in.setInputs(prompt.toString());
                TokenIterator it = predictor.predict(in);
                while (it.hasNext()) {
                    Token token = it.next();
                    System.out.print(token.getText());
                    prompt.append(token.getText());
                }
            }
        }
    }
}
