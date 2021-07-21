/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SentimentAnalysis {

    private static final String TOPIC = "twitter-data";

    public static void main(String[] args)
            throws MalformedModelException, ModelNotFoundException, IOException {
        // Loading Model from DJL Model Zoo
        // Specify criteria to find target model
        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                        .setTypes(String.class, Classifications.class)
                        .build();
        // Load model
        ZooModel<String, Classifications> model = criteria.loadModel();
        // Create predictor
        Predictor<String, Classifications> predictor = model.newPredictor();

        int numConsumers = 3;
        List<String> topics = Collections.singletonList(TOPIC);
        ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

        // setup consumer
        final List<ConsumerLoop> consumers = new ArrayList<>();
        for (int i = 0; i < numConsumers; i++) {
            ConsumerLoop consumer = new ConsumerLoop(i, topics, predictor);
            consumers.add(consumer);
            executor.submit(consumer);
        }

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    for (ConsumerLoop consumer : consumers) {
                                        consumer.shutdown();
                                    }
                                    executor.shutdown();
                                    try {
                                        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }));
    }
}
