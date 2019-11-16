package com.example;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.metric.Metrics;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.convolutional.Conv1D;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.Dropout;
import ai.djl.nn.pooling.Pool;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** Wraps Model object, definition. loading and training methods into a singleton object */
class MaliciousURLModel {
    private static MaliciousURLModel maliciousURLModel;
    private String actualModelName;
    private String modelDir;

    Model getModel() {
        return model;
    }

    private Model model;

    private MaliciousURLModel() {
        actualModelName = "maliciousURLCNNModel";
        modelDir = "src/main/resources";
    }

    /**
     * Define the imperative model, returns a model object will all the layers sets model object to
     * the blocks Used for Bot training and inference.
     */
    void defineModel() {
        SequentialBlock block = new SequentialBlock();
        float dropoutProbability = (float) 0.5;
        int fullyConnected = 1024;
        int numberOfFilters = 256;
        block.add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(7))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        ndList ->
                                new NDList(
                                        Pool.maxPool(
                                                ndList.singletonOrThrow(),
                                                new Shape(3),
                                                new Shape(3),
                                                new Shape(0))))
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(7))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        ndList ->
                                new NDList(
                                        Pool.maxPool(
                                                ndList.singletonOrThrow(),
                                                new Shape(3),
                                                new Shape(3),
                                                new Shape(0))))
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        ndList ->
                                new NDList(
                                        Pool.maxPool(
                                                ndList.singletonOrThrow(),
                                                new Shape(3),
                                                new Shape(3),
                                                new Shape(0))))
                .add(Blocks.batchFlattenBlock())
                .add(new Linear.Builder().setOutChannels(fullyConnected).build())
                .add(Activation.reluBlock())
                .add(new Dropout.Builder().optProbability(dropoutProbability).build())
                .add(new Linear.Builder().setOutChannels(fullyConnected).build())
                .add(Activation.reluBlock())
                .add(new Dropout.Builder().optProbability(dropoutProbability).build())
                .add(new Linear.Builder().setOutChannels(2).build());

        model = Model.newInstance();
        model.setBlock(block);
    }

    static MaliciousURLModel getInstance() {
        if (maliciousURLModel == null) {
            maliciousURLModel = new MaliciousURLModel();
        }
        return maliciousURLModel;
    }

    /** Load model. Note to call defineModel before calling this method, during inference. */
    void loadModel() throws IOException, MalformedModelException {
        Path modelPath = Paths.get(modelDir);
        model.load(modelPath, actualModelName);
    }
    /**
     * Inference on loaded model. call loadModel before this. Call flow defineModel -> loadModel ->
     * inference Calls URL Translator for pre-process and post-process functionality
     */
    Classifications inference(String url) {
        Metrics metrics = new Metrics();
        URLTranslator urlTranslator = new URLTranslator();
        try (Predictor<String, List<Classifications>> predictor =
                model.newPredictor(urlTranslator)) {
            predictor.setMetrics(metrics); // Let predictor collect metrics
            return predictor.predict(url).get(0);

        } catch (TranslateException e) {
            throw new RuntimeException(e);
        } finally {
            model.close();
        }
    }
}
