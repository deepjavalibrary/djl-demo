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
package org.example.training;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import org.example.training.listener.CheckpointsTrainingListener;
import org.example.training.listener.UiTrainingListener;

import java.io.IOException;
import java.nio.file.Paths;

public final class MnistTraining {

    private static final String MODEL_DIR = "./model/mnist";
    private static final String MODEL_NAME = "mlp";

    public static void main(String[] args) throws Exception {
        train(MODEL_DIR);
    }

    public static void train(String modelDir) throws Exception {
        // Construct neural network
        Block block = new Mlp(Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH, Mnist.NUM_CLASSES, new int[]{128, 64});

        try (Model model = Model.newInstance(MODEL_NAME)) {
            model.setBlock(block);

            // get training and validation dataset
            RandomAccessDataset trainingSet = prepareDataset(Dataset.Usage.TRAIN, 64, Long.MAX_VALUE);
            RandomAccessDataset validateSet = prepareDataset(Dataset.Usage.TEST, 64, Long.MAX_VALUE);

            // setup training configuration
            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .addEvaluator(new Accuracy())
                    .addTrainingListeners(TrainingListener.Defaults.logging())
                    .addTrainingListeners(new UiTrainingListener())
                    .addTrainingListeners(new CheckpointsTrainingListener(modelDir));

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.setMetrics(new Metrics());

                Shape inputShape = new Shape(1, Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH);

                // initialize trainer with proper input shape
                trainer.initialize(inputShape);
                EasyTrain.fit(trainer, 10, trainingSet, validateSet);
            }
            model.save(Paths.get(modelDir), MODEL_NAME);
        }
    }

    private static RandomAccessDataset prepareDataset(Dataset.Usage usage, int batchSize, long limit) throws IOException {
        Mnist mnist = Mnist.builder().optUsage(usage).setSampling(batchSize, true).optLimit(limit).build();
        mnist.prepare(new ProgressBar());
        return mnist;
    }
}
