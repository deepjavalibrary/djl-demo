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
package com.example.inferentia;

import ai.djl.ModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.translate.TranslatorContext;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Benchmark {

    private static final Logger logger = LoggerFactory.getLogger(Benchmark.class);

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        String modelPath;
        if (args.length > 0) {
            modelPath = args[0];
        } else {
            modelPath = "models/inferentia/resnet50";
        }
        int numOfThreads = Runtime.getRuntime().availableProcessors();
        if (args.length > 1) {
            numOfThreads = Integer.parseInt(args[1]);
        }
        int iterations = 1000;
        if (args.length > 2) {
            iterations = Integer.parseInt(args[2]);
        }

        String version = Engine.getEngine("PyTorch").getVersion();
        logger.info("Running inference with PyTorch: {}", version);

        if (modelPath.contains("inferentia")) {
            String extraPath = System.getenv("PYTORCH_EXTRA_LIBRARY_PATH");
            if (extraPath != null) {
                logger.info("Loading libneuron_op.so from: {}", extraPath);
                System.load(extraPath);
            } else {
                logger.info("Loading libneuron_op.so");
                System.loadLibrary("neuron_op");
            }
        } else {
            logger.info("Loading regular pytorch model ...");
        }

        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelPath(Paths.get(modelPath))
                        .optTranslator(getTranslator())
                        .build();

        Metrics metrics = new Metrics();
        try (ZooModel<NDList, NDList> model = criteria.loadModel()) {
            singleThreadInference(model, null, 1); // warm up
            if (numOfThreads == 1) {
                singleThreadInference(model, metrics, iterations);
            } else {
                metrics = new Metrics();
                iterations *= numOfThreads;
                multiThreadInference(model, metrics, numOfThreads, iterations);
            }
        }
        printResult(metrics, iterations);
    }

    private static void singleThreadInference(
            ZooModel<NDList, NDList> model, Metrics metrics, int iteration)
            throws TranslateException {
        try (Predictor<NDList, NDList> predictor = model.newPredictor()) {
            predictor.setMetrics(metrics);
            long begin = System.currentTimeMillis();
            for (int i = 0; i < iteration; ++i) {
                predictor.predict(null);
            }
            long duration = System.currentTimeMillis() - begin;
            if (metrics != null) {
                metrics.addMetric("duration", duration, "mills");
            }
        }
    }

    private static void multiThreadInference(
            ZooModel<NDList, NDList> model, Metrics metrics, int numOfThreads, int iteration) {
        AtomicInteger counter = new AtomicInteger(iteration);
        logger.info("Multithreaded inference with {} threads.", numOfThreads);

        List<PredictorCallable> callables = new ArrayList<>(numOfThreads);
        for (int i = 0; i < numOfThreads; ++i) {
            callables.add(new PredictorCallable(model, metrics, counter));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
        try {
            long begin = System.currentTimeMillis();
            try {
                List<Future<Object>> futures = executorService.invokeAll(callables);
                for (Future<Object> future : futures) {
                    future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error("", e);
            }
            long duration = System.currentTimeMillis() - begin;
            metrics.addMetric("duration", duration, "mills");

            for (PredictorCallable callable : callables) {
                callable.close();
            }
        } finally {
            executorService.shutdown();
        }
    }

    private static void printResult(Metrics metrics, int iteration) {
        long totalTime = metrics.getMetric("duration").get(0).getValue().longValue();
        String throughput = String.format("%.2f", iteration * 1000d / totalTime);
        logger.info(
                "Throughput: {}, completed {} iteration in {} ms.",
                throughput,
                iteration,
                totalTime);

        float totalP50 = metrics.percentile("Total", 50).getValue().longValue() / 1_000_000f;
        float totalP90 = metrics.percentile("Total", 90).getValue().longValue() / 1_000_000f;
        float totalP99 = metrics.percentile("Total", 99).getValue().longValue() / 1_000_000f;
        logger.info(
                String.format(
                        "Latency P50: %.3f ms, P90: %.3f ms, P99: %.3f ms",
                        totalP50, totalP90, totalP99));
    }

    private static NoopTranslator getTranslator() {
        return new NoopTranslator() {
            @Override
            public NDList processInput(TranslatorContext ctx, NDList input) {
                return new NDList(ctx.getNDManager().zeros(new Shape(1, 3, 224, 224)));
            }
        };
    }

    private static class PredictorCallable implements Callable<Object> {

        private Predictor<NDList, NDList> predictor;

        private Metrics metrics;
        private AtomicInteger counter;

        public PredictorCallable(
                ZooModel<NDList, NDList> model, Metrics metrics, AtomicInteger counter) {
            this.predictor = model.newPredictor();
            predictor.setMetrics(metrics);
            this.metrics = metrics;
            this.counter = counter;
        }

        /** {@inheritDoc} */
        @Override
        public Object call() throws Exception {
            while (counter.decrementAndGet() > 0) {
                try {
                    predictor.predict(null);
                } catch (Exception e) {
                    // stop immediately when we find any exception
                    counter.set(0);
                    throw e;
                }
            }
            return null;
        }

        public void close() {
            predictor.close();
        }
    }
}
