package org.example;

import ai.djl.Model;
import ai.djl.basicdataset.Mnist;
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
import ai.djl.training.listener.CheckpointsTrainingListener;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import ai.djl.ui.listener.UiTrainingListener;
import io.vavr.control.Try;

import java.io.IOException;
import java.nio.file.Paths;

public final class MnistTraining {

    private static final String MODEL_DIR = "target/models/mnist";
    private static final String MODEL_NAME = "mlp";

    public static void main(String[] args) throws IOException {
        // Construct neural network
        Block block = new Mlp(Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH, Mnist.NUM_CLASSES, new int[]{128, 64});

        Model model = Try.of(() -> Model.newInstance(MODEL_NAME))
                .map(newModel -> setBlock(newModel, block))
                // Rethrow exceptions
                .get();

        // get training and validation dataset
        RandomAccessDataset trainingSet = prepareDataset(Dataset.Usage.TRAIN, 64, Long.MAX_VALUE);
        RandomAccessDataset validateSet = prepareDataset(Dataset.Usage.TEST, 64, Long.MAX_VALUE);

        // setup training configuration
        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging())
                .addTrainingListeners(new UiTrainingListener())
                .addTrainingListeners(new CheckpointsTrainingListener(MODEL_DIR));

        Shape inputShape = new Shape(1, Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH);

        Metrics metrics = new Metrics();

        Trainer trainer = Try.of(() -> model.newTrainer(config))
                .map(newTrainer -> setMetrics(newTrainer, metrics))
                .map(newTrainer -> initialize(newTrainer, inputShape))
                // Rethrow exceptions
                .get();

        Try.run(() -> EasyTrain.fit(trainer, 10, trainingSet, validateSet))
                // Rethrow exceptions
                .get();

        model.save(Paths.get(MODEL_DIR), MODEL_NAME);
    }

    private static Model setBlock(Model model, Block block) {
        model.setBlock(block);
        return model;
    }

    private static Trainer setMetrics(Trainer trainer, Metrics metrics) {
        trainer.setMetrics(new Metrics());
        return trainer;
    }

    private static Trainer initialize(Trainer trainer, Shape shape) {
        trainer.initialize(shape);
        return trainer;
    }

    private static RandomAccessDataset prepareDataset(Dataset.Usage usage, int batchSize, long limit) throws IOException {
        Mnist mnist = Mnist.builder().optUsage(usage).setSampling(batchSize, true).optLimit(limit).build();
        mnist.prepare(new ProgressBar());
        return mnist;
    }
}
