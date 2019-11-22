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
import ai.djl.ndarray.types.Shape;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import java.io.IOException;
import java.io.Reader;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVDataset extends RandomAccessDataset {
    private String allChars =
            "abcdefghijklmnopqrstuvwxyz0123456789-,;.!?:'\"/\\|_@#$%^&*~`+ =<>()[]{}";
    private static final int FEATURE_LENGTH = 1014;
    private List<Character> alphabets;
    private Map<Character, Integer> alphabetsIndex;
    private NDManager manager;
    private Usage usage;
    private List<CSVRecord> csvRecords;
    private List<CSVRecord> subRecord;
    private int size;

    private Shape initializeShape;
    /**
     * Reads CSV File and sets up information for encoding.
     *
     * @param builder Builder subclass for building the dataset
     */
    private CSVDataset(Builder builder) {
        super(builder);
        this.manager = builder.manager;
        this.usage = builder.usage;
        // Load CSV dataset into CSV REcords
        String csvFileLocation = "src/main/resources/malicious_url_data.csv";
        // set encoding base information
        alphabets = allChars.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        alphabetsIndex =
                IntStream.range(0, alphabets.size()).boxed().collect(toMap(alphabets::get, i -> i));
        try (Reader reader = Files.newBufferedReader(Paths.get(csvFileLocation));
                CSVParser csvParser =
                        new CSVParser(
                                reader,
                                CSVFormat.DEFAULT
                                        .withHeader("url", "isMalicious")
                                        .withIgnoreHeaderCase()
                                        .withTrim()); ) {
            csvRecords = csvParser.getRecords();
            csvRecords.remove(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // For use with Trainer initializer
        initializeShape = new Shape(1, alphabets.size(), FEATURE_LENGTH);
    }

    public Shape getInitializeShape() {
        return initializeShape;
    }

    public static Builder builder(NDManager manager) {
        return new Builder().setManager(manager);
    }

    /** {@inheritDoc} */
    @Override
    public Record get(NDManager manager, long index) {
        NDList datum = new NDList();
        NDList label = new NDList();
        CSVRecord record = subRecord.get((int) index);
        // Get a single data, label pair, encode them using helpers
        datum.add(encodeData(record.get("url")));
        label.add(encodeLabel(record.get("isMalicious")));
        datum.attach(manager);
        label.attach(manager);
        return new Record(datum, label);
    }

    /** {@inheritDoc} */
    @Override
    public long size() {
        if (size == 0) {
            throw new RuntimeException(" call PrepareData() before calling size()");
        }
        return size;
    }

    /**
     * Convert the URL string to NDArray encoded form
     *
     * @param url URL in string format
     */
    private NDArray encodeData(String url) {
        FloatBuffer buf = FloatBuffer.allocate(alphabets.size() * FEATURE_LENGTH);
        char[] arrayText = url.toCharArray();
        for (int i = 0; i < url.length(); i++) {
            if (i > FEATURE_LENGTH) {
                break;
            }
            if (alphabetsIndex.containsKey(arrayText[i])) {
                int index = arrayText[i] * alphabets.size() + i;
                buf.put(index, 1);
            }
        }
        return manager.create(buf, new Shape(alphabets.size(), FEATURE_LENGTH));
    }
    /**
     * Convert the label string to NDArray encoded form
     *
     * @param isMalicious indicating if sample is malicious or not (label)
     */
    private NDArray encodeLabel(String isMalicious) {
        return manager.create(Float.parseFloat(isMalicious));
    }

    /**
     * Divide CSVRecords to TRAIN and TEST datasets Needed subuset is set
     *
     * @param usage TRAIN or TEST usage
     */
    void prepareData(Usage usage) {
        // 80% of records for TRAIN rest for TEST
        Double temp = csvRecords.size() * 0.8;
        int splitIndex = temp.intValue();
        switch (usage) {
            case TRAIN:
                {
                    subRecord = csvRecords.subList(0, splitIndex);
                    size = subRecord.size();
                    break;
                }
            case TEST:
                {
                    subRecord = csvRecords.subList(splitIndex, csvRecords.size());
                    size = subRecord.size();
                    break;
                }
            default:
                {
                    throw new RuntimeException("Wrong usage passed");
                }
        }
    }

    public static final class Builder extends BaseBuilder<Builder> {
        private NDManager manager;
        private Usage usage;

        public Builder() {
            this.usage = Usage.TRAIN;
        }

        protected Builder self() {
            return this;
        }

        public Builder setManager(NDManager manager) {
            this.manager = manager.newSubManager();
            return this;
        }

        public Builder optUsage(Usage usage) {
            this.usage = usage;
            return this;
        }

        public CSVDataset build() {
            return new CSVDataset(this);
        }
    }
}
