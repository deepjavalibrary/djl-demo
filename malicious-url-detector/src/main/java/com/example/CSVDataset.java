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

import static java.util.stream.Collectors.toMap;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import ai.djl.util.Progress;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVDataset extends RandomAccessDataset {

    private static final Logger logger = LoggerFactory.getLogger(CSVDataset.class);

    private static final int FEATURE_LENGTH = 1014;
    private static final String ALL_CHARS =
            "abcdefghijklmnopqrstuvwxyz0123456789-,;.!?:'\"/\\|_@#$%^&*~`+ =<>()[]{}";

    private List<Character> alphabets;
    private Map<Character, Integer> alphabetsIndex;
    private List<CSVRecord> dataset;

    /**
     * Reads CSV File and sets up information for encoding.
     *
     * @param builder Builder subclass for building the dataset
     */
    private CSVDataset(Builder builder) {
        super(builder);
        dataset = builder.dataset;
        // Load CSV dataset into CSV REcords
        // set encoding base information
        alphabets = ALL_CHARS.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        alphabetsIndex =
                IntStream.range(0, alphabets.size()).boxed().collect(toMap(alphabets::get, i -> i));
    }

    public static Shape getInitializeShape() {
        return new Shape(1, ALL_CHARS.length(), FEATURE_LENGTH);
    }

    /** {@inheritDoc} */
    @Override
    public Record get(NDManager manager, long index) {
        NDList datum = new NDList();
        NDList label = new NDList();
        CSVRecord record = dataset.get(Math.toIntExact(index));
        // Get a single data, label pair, encode them using helpers
        datum.add(encodeData(manager, record.get("url")));
        label.add(encodeLabel(manager, record.get("isMalicious")));
        return new Record(datum, label);
    }

    /** {@inheritDoc} */
    @Override
    protected long availableSize() {
        return dataset.size();
    }

    /**
     * Convert the URL string to NDArray encoded form
     *
     * @param manager NDManager for NDArray context
     * @param url URL in string format
     */
    private NDArray encodeData(NDManager manager, String url) {
        NDArray encoded = manager.zeros(new Shape(alphabets.size(), FEATURE_LENGTH));
        char[] arrayText = url.toCharArray();
        for (int i = 0; i < url.length(); i++) {
            if (i > FEATURE_LENGTH) {
                break;
            }
            if (alphabetsIndex.containsKey(arrayText[i])) {
                encoded.set(new NDIndex(alphabetsIndex.get(arrayText[i]), i), 1);
            }
        }
        return encoded;
    }
    /**
     * Convert the label string to NDArray encoded form
     *
     * @param manager NDManager for NDArray context
     * @param isMalicious indicating if sample is malicious or not (label)
     */
    private NDArray encodeLabel(NDManager manager, String isMalicious) {
        return manager.create(Float.parseFloat(isMalicious));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void prepare(Progress progress) {}

    public static final class Builder extends BaseBuilder<Builder> {

        List<CSVRecord> dataset;

        protected Builder self() {
            return this;
        }

        CSVDataset build() throws IOException {
            Path path = Paths.get("dataset");
            Files.createDirectories(path);
            Path csvFile = path.resolve("malicious_url_data.csv");
            if (!Files.exists(csvFile)) {
                logger.info("Downloading dataset file ...");
                URL url =
                        new URL(
                                "https://raw.githubusercontent.com/incertum/cyber-matrix-ai/master/Malicious-URL-Detection-Deep-Learning/data/url_data_mega_deep_learning.csv");
                Files.copy(url.openStream(), csvFile);
            }

            try (Reader reader = Files.newBufferedReader(csvFile);
                    CSVParser csvParser =
                            new CSVParser(
                                    reader,
                                    CSVFormat.DEFAULT
                                            .withHeader("url", "isMalicious")
                                            .withFirstRecordAsHeader()
                                            .withIgnoreHeaderCase()
                                            .withTrim())) {
                dataset = csvParser.getRecords();
                return new CSVDataset(this);
            }
        }
    }
}
