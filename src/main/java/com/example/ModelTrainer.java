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

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.metric.Metric;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.TrainingListener;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.initializer.XavierInitializer;
import ai.djl.training.loss.Loss;
import ai.djl.training.metrics.Accuracy;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.optimizer.learningrate.FactorTracker;
import ai.djl.training.optimizer.learningrate.LearningRateTracker;
import ai.djl.training.util.ProgressBar;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelTrainer implements TrainingListener {
    private Model model;
    private int trainDataSize;
    private int validateDataSize;
    private final int batchSize = 128;
    private Loss loss;
    private Metrics metrics;

    private long epochTime;
    private int numEpochs;
    private float trainingAccuracy;
    private float trainingLoss;
    private float validationAccuracy;
    private int trainingProgress;
    private int validateProgress;

    private Logger logger = LoggerFactory.getLogger(ModelTrainer.class);
    private ProgressBar trainingProgressBar;
    private ProgressBar validateProgressBar;
    private Shape initializeShape;

    public static void main(String[] args) {
        ModelTrainer modelTrainer = new ModelTrainer();
        modelTrainer.train();
    }

    /** Get a instance of model and perpare it for training, create metrics object. */
    private ModelTrainer() {
        MaliciousURLModel maliciousURLModel = MaliciousURLModel.getInstance();
        maliciousURLModel.defineModel();
        model = maliciousURLModel.getModel();
        metrics = new Metrics();
    }

    /**
     * Get dataset for use-case TRAIN or TEST. Look at CSVDataset class for split details.
     *
     * @param manager manager of arrays in context.
     * @param usage defines dataset usage TRAIN or TEST
     * @return Dataset object for given usage.
     */
    private Dataset getDataset(NDManager manager, Dataset.Usage usage) {
        CSVDataset csvDataset =
                CSVDataset.builder(manager).optUsage(usage).setSampling(batchSize, true).build();
        if (initializeShape == null) {
            initializeShape = csvDataset.getInitializeShape();
        }

        csvDataset.prepareData(usage);
        if (usage == Dataset.Usage.TRAIN) {
            trainDataSize = (int) csvDataset.size() / batchSize;
        } else {
            validateDataSize = (int) csvDataset.size() / batchSize;
        }
        return csvDataset;
    }

    /** Calls training, loop, sets up trainer, and saves model at end of epoch runs. */
    private void train() {
        try {
            logger.info("Loading Dataset");
            Dataset trainingDataset = getDataset(model.getNDManager(), Dataset.Usage.TRAIN);
            Dataset validateDataset = getDataset(model.getNDManager(), Dataset.Usage.TEST);
            TrainingConfig config = setupTraining();

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.setMetrics(metrics);
                trainer.setTrainingListener(this);

                /*
                 * MNIST is 28x28 grayscale image and pre processed into 28 * 28 NDArray.
                 * 1st axis is batch axis, we can use 1 for initialization.
                 */
                logger.info("Initialize Trainer");
                // initialize trainer with proper input shape
                trainer.initialize(initializeShape);
                logger.info("Begin Training");
                int epoch = 10;
                TrainingUtils.fit(
                        trainer,
                        epoch,
                        trainingDataset,
                        validateDataset,
                        System.getProperty("user.dir"));
            }

            // save model
            model.setProperty("Epoch", String.valueOf(numEpochs));
            model.setProperty("Accuracy", String.format("%.2f", getValidationAccuracy()));
            TrainingUtils.dumpTrainingTimeInfo(metrics, System.getProperty("user.dir"));
            model.save(Paths.get("src/main/resources/"), "maliciousURLCNNModel");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets training parameters, like learning rates, optimizer.
     *
     * @return a TrainingConfig object to initialize trainer.
     */
    private TrainingConfig setupTraining() {
        FactorTracker factorTracker =
                LearningRateTracker.factorTracker()
                        .optBaseLearningRate(0.01f)
                        .setStep(trainDataSize)
                        .build();
        Optimizer optimizer =
                Optimizer.sgd()
                        .setRescaleGrad(1.0f / batchSize)
                        .setLearningRateTracker(factorTracker)
                        .optWeightDecays(0.00001f)
                        .optMomentum(0.9f)
                        .build();

        loss = Loss.softmaxCrossEntropyLoss();
        return new DefaultTrainingConfig(
                        new XavierInitializer(
                                XavierInitializer.RandomType.UNIFORM,
                                XavierInitializer.FactorType.AVG,
                                2.24),
                        loss)
                .setOptimizer(optimizer)
                .addTrainingMetric(new Accuracy())
                .setBatchSize(batchSize)
                .setDevices(new Device[] {Device.defaultDevice()});
    }
    // The below or helper functions to display and records metrics for training.
    /** {@inheritDoc} */
    @Override
    public void onEpoch() {
        if (epochTime > 0L) {
            metrics.addMetric("epoch", System.nanoTime() - epochTime);
        }
        logger.info("Epoch " + numEpochs + " finished.");
        printTrainingStatus(metrics);

        epochTime = System.nanoTime();
        numEpochs++;
        trainingProgress = 0;
        validateProgress = 0;
    }

    private float getValidationAccuracy() {
        return validationAccuracy;
    }
    /** {@inheritDoc} */
    @Override
    public void onTrainingBatch() {
        MemoryUtils.collectMemoryInfo(metrics);
        if (trainingProgressBar == null) {
            trainingProgressBar = new ProgressBar("Training", trainDataSize);
        }
        trainingProgressBar.update(trainingProgress++, getTrainingStatus(metrics));
    }

    /** {@inheritDoc} */
    @Override
    public void onValidationBatch() {
        MemoryUtils.collectMemoryInfo(metrics);
        if (validateProgressBar == null) {
            validateProgressBar = new ProgressBar("Validating", validateDataSize);
        }
        validateProgressBar.update(validateProgress++);
    }

    private String getTrainingStatus(Metrics metrics) {
        StringBuilder sb = new StringBuilder();
        List<Metric> list = metrics.getMetric("train_" + loss.getName());
        trainingLoss = list.get(list.size() - 1).getValue().floatValue();

        list = metrics.getMetric("train_Accuracy");
        trainingAccuracy = list.get(list.size() - 1).getValue().floatValue();
        // use .2 precision to avoid new line in progress bar
        sb.append(String.format("accuracy: %.2f loss: %.2f", trainingAccuracy, trainingLoss));

        list = metrics.getMetric("train");
        if (!list.isEmpty()) {
            float batchTime = list.get(list.size() - 1).getValue().longValue() / 1_000_000_000f;
            sb.append(String.format(" speed: %.2f urls/sec", (float) batchSize / batchTime));
        }
        return sb.toString();
    }

    private void printTrainingStatus(Metrics metrics) {
        List<Metric> list = metrics.getMetric("train_" + loss.getName());
        trainingLoss = list.get(list.size() - 1).getValue().floatValue();

        list = metrics.getMetric("train_Accuracy");
        trainingAccuracy = list.get(list.size() - 1).getValue().floatValue();

        logger.info("train accuracy: {}, train loss: {}", trainingAccuracy, trainingLoss);
        list = metrics.getMetric("validate_" + loss.getName());
        if (!list.isEmpty()) {
            float validationLoss = list.get(list.size() - 1).getValue().floatValue();
            list = metrics.getMetric("validate_Accuracy");
            validationAccuracy = list.get(list.size() - 1).getValue().floatValue();

            logger.info(
                    "validate accuracy: {}, validate loss: {}", validationAccuracy, validationLoss);
        } else {
            logger.info("validation has not been run.");
        }
    }
}
