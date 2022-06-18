/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.Arrays;

/**
 * Implements a streaming version of the sentence encoder program. Using TensorFlow model from
 * TFHub: https://tfhub.dev/google/universal-sentence-encoder/4 Accepts a String input and returns a
 * float array as its embedding.
 *
 * <p>This program connects to a server socket and reads strings from the socket. The easiest way to
 * try this out is to open a text server (at port 12345) using the <i>netcat</i> tool via
 *
 * <pre>
 * nc -l 12345 on Linux or nc -l -p 12345 on Windows
 * </pre>
 *
 * <p>and run this example with the hostname and the port as arguments.
 */
public class SentenceEncoder {

    public static void main(String[] args) throws Exception {

        // the host and the port to connect to
        final String hostname;
        final int port;
        try {
            final ParameterTool params = ParameterTool.fromArgs(args);
            hostname = params.has("hostname") ? params.get("hostname") : "localhost";
            port = params.getInt("port");
        } catch (Exception e) {
            System.err.println(
                    "No port specified. Please run 'SentimentAnalysis --hostname <hostname> --port"
                        + " <port>', where hostname (localhost by default) and port is the address"
                        + " of the text server");
            System.err.println(
                    "To start a simple text server, run 'netcat -l <port>' and "
                            + "type the input text into the command line");
            return;
        }

        // get the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // get input data by connecting to the socket
        DataStream<String> text = env.socketTextStream(hostname, port, "\n");

        // Run inference with Flink streaming
        DataStream<String> embedding = text.flatMap(new SEFlatMap());

        // print the results with a single thread, rather than in parallel
        embedding.print().setParallelism(1);
        env.execute("SentimentAnalysis");
    }

    /** Sentiment Analysis {@link FlatMapFunction} implementation. */
    public static class SEFlatMap implements FlatMapFunction<String, String> {

        private static Predictor<String, float[]> predictor;

        private Predictor<String, float[]> getOrCreatePredictor()
                throws ModelException, IOException {
            if (predictor == null) {
                String modelUrl =
                        "https://storage.googleapis.com/tfhub-modules/google/universal-sentence-encoder/4.tar.gz";

                Criteria<String, float[]> criteria =
                        Criteria.builder()
                                .optApplication(Application.NLP.TEXT_EMBEDDING)
                                .setTypes(String.class, float[].class)
                                .optModelUrls(modelUrl)
                                .optTranslator(new MyTranslator())
                                .optProgress(new ProgressBar())
                                .build();
                ZooModel<String, float[]> model = criteria.loadModel();
                predictor = model.newPredictor();
            }
            return predictor;
        }

        @Override
        public void flatMap(String value, Collector<String> out) throws Exception {
            Predictor<String, float[]> predictor = getOrCreatePredictor();
            out.collect(Arrays.toString(predictor.predict(value)));
        }
    }

    private static final class MyTranslator implements Translator<String, float[]> {

        MyTranslator() {}

        @Override
        public NDList processInput(TranslatorContext ctx, String inputs) {
            NDManager manager = NDManager.newBaseManager();
            return new NDList(manager.create(inputs).expandDims(0));
        }

        @Override
        public float[] processOutput(TranslatorContext ctx, NDList list) {
            return list.singletonOrThrow().get(0).toFloatArray();
        }

        @Override
        public Batchifier getBatchifier() {
            return null;
        }
    }
}
