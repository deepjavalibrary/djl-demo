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
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.io.IOException;

/**
 * Implements a streaming version of the sentiment analysis program.
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
public class SentimentAnalysis {

    public static void main(String[] args) throws Exception {
        // the host and the port to connect to
        String hostname;
        int port;
        try {
            ParameterTool params = ParameterTool.fromArgs(args);
            hostname = params.has("hostname") ? params.get("hostname") : "localhost";
            port = params.has("port") ? params.getInt("port") : 9000;
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
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // get input data by connecting to the socket
        DataStream<String> text = env.socketTextStream(hostname, port, "\n");

        // Run inference with Flink streaming
        DataStream<Classifications> classifications = text.flatMap(new SAFlatMap());

        // print the results with a single thread, rather than in parallel
        classifications.print().setParallelism(1);
        env.execute("SentimentAnalysis");
    }

    /** Sentiment Analysis {@link FlatMapFunction} implementation. */
    public static class SAFlatMap implements FlatMapFunction<String, Classifications> {

        private static Predictor<String, Classifications> predictor;

        private Predictor<String, Classifications> getOrCreatePredictor()
                throws ModelException, IOException {
            if (predictor == null) {
                Criteria<String, Classifications> criteria =
                        Criteria.builder()
                                .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                                .setTypes(String.class, Classifications.class)
                                .optProgress(new ProgressBar())
                                .build();
                ZooModel<String, Classifications> model = criteria.loadModel();
                predictor = model.newPredictor();
            }
            return predictor;
        }

        @Override
        public void flatMap(String value, Collector<Classifications> out) throws Exception {
            Predictor<String, Classifications> predictor = getOrCreatePredictor();
            Classifications classifications = predictor.predict(value);
            out.collect(classifications);
        }
    }
}
