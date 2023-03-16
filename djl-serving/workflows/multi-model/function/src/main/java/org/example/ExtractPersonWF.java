/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package org.example;

import ai.djl.modality.Output;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.ndarray.BytesSupplier;
import ai.djl.serving.workflow.Workflow;
import ai.djl.serving.workflow.WorkflowExpression;
import ai.djl.serving.workflow.function.WorkflowFunction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Workflow function "extractPerson" accepts an input image and detected objects, then returns the
 * cropped person image.
 */
public class ExtractPersonWF extends WorkflowFunction {

    private static final Gson GSON =
            new GsonBuilder()
                    .registerTypeAdapter(DetectedObjects.class, new DetectedObjectsDeserializer())
                    .create();

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<WorkflowExpression.Item> run(
            Workflow.WorkflowExecutor executor, List<Workflow.WorkflowArgument> args) {
        if (args.size() != 2) {
            throw new IllegalArgumentException("Expected two arguments to extractPerson");
        }
        return evaluateArgs(args)
                .thenApply(
                        pa -> {
                            try {
                                Output output = new Output();
                                BytesSupplier imageData = pa.get(0).getInput().getData();
                                BytesSupplier detectData = pa.get(1).getInput().getData();
                                if (imageData == null || detectData == null) {
                                    throw new RuntimeException("Input data is empty");
                                }
                                Image image =
                                        ImageFactory.getInstance()
                                                .fromInputStream(
                                                        new ByteArrayInputStream(
                                                                imageData.getAsBytes()));
                                DetectedObjects detectedObjects =
                                        GSON.fromJson(
                                                detectData.getAsString(), DetectedObjects.class);
                                List<DetectedObjects.DetectedObject> items =
                                        detectedObjects.items();
                                for (DetectedObjects.DetectedObject item : items) {
                                    if ("person".equals(item.getClassName())) {
                                        Rectangle rect = item.getBoundingBox().getBounds();
                                        int width = image.getWidth();
                                        int height = image.getHeight();
                                        Image sub =
                                                image.getSubImage(
                                                        (int) (rect.getX() * width),
                                                        (int) (rect.getY() * height),
                                                        (int) (rect.getWidth() * width),
                                                        (int) (rect.getHeight() * height));
                                        try (ByteArrayOutputStream bos =
                                                new ByteArrayOutputStream()) {
                                            sub.save(bos, "png");
                                            output.add(bos.toByteArray());
                                        }
                                        output.addProperty("Content-Type", "image/png");
                                        break;
                                    }
                                }
                                return new WorkflowExpression.Item(output);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }
}
