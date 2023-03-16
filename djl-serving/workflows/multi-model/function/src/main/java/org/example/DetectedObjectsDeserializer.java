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

import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** A customized Gson deserializer to deserialize the {@code DetectedObjects} object. */
public class DetectedObjectsDeserializer implements JsonDeserializer<DetectedObjects> {

    /** {@inheritDoc} */
    @Override
    public DetectedObjects deserialize(
            JsonElement json, Type member, JsonDeserializationContext context) {
        List<String> classNames = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        JsonArray array = json.getAsJsonArray();
        for (JsonElement element : array) {
            JsonObject item = element.getAsJsonObject();
            classNames.add(item.get("className").getAsString());
            probabilities.add(item.get("probability").getAsDouble());
            boundingBoxes.add(context.deserialize(item.get("boundingBox"), Rectangle.class));
        }
        return new DetectedObjects(classNames, probabilities, boundingBoxes);
    }
}
