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

import ai.djl.Model;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.dataset.Batch;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import java.io.IOException;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelTrainer {

    private static final Logger logger = LoggerFactory.getLogger(ModelTrainer.class);

    private static final int BATCH_SIZE = 128;
    private static final int EPOCH = 7;

    public static void main(String[] args) throws IOException {
        MaliciousURLModel maliciousURLModel = MaliciousURLModel.getInstance();
        maliciousURLModel.defineModel();
        Model model = maliciousURLModel.getModel();

        logger.info("Loading Dataset");
        CSVDataset csvDataset = CSVDataset.builder().setSampling(BATCH_SIZE, true).build();
        RandomAccessDataset[] datasets = csvDataset.randomSplit(8, 2);

        TrainingConfig config =
                new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                        .addEvaluator(new Accuracy())
                        .setBatchSize(BATCH_SIZE)
                        .addTrainingListeners(
                                TrainingListener.Defaults.logging(
                                        "Malicious URL training",
                                        BATCH_SIZE,
                                        (int) datasets[0].getNumIterations(),
                                        (int) datasets[1].getNumIterations(),
                                        null));

        try (Trainer trainer = model.newTrainer(config)) {
            // initialize trainer with proper input shape
            trainer.initialize(CSVDataset.getInitializeShape());
            logger.info("Begin Training");

            for (int epoch = 0; epoch < EPOCH; epoch++) {
                for (Batch batch : trainer.iterateDataset(datasets[0])) {
                    trainer.trainBatch(batch);
                    trainer.step();
                    batch.close();
                }

                for (Batch batch : trainer.iterateDataset(datasets[1])) {
                    trainer.validateBatch(batch);
                    batch.close();
                }

                // reset training and validation metric at end of epoch
                trainer.endEpoch();
            }
        }

        // save model
        model.setProperty("Epoch", String.valueOf(EPOCH));
        model.save(Paths.get("model"), "maliciousURLCNNModel");
    }
}
