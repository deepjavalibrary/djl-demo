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
package com.examples;

import java.util.HashMap;
import java.util.Map;

public class Request {

    private String inputImageUrl;
    private String artifactId;
    private Map<String, String> filters;

    public String getInputImageUrl() {
        return inputImageUrl;
    }

    public void setInputImageUrl(String inputImageUrl) {
        this.inputImageUrl = inputImageUrl;
    }

    public String getArtifactId() {
        if (artifactId == null) {
            artifactId = "ai.djl.mxnet:resnet";
            if (filters == null) {
                filters = new HashMap<>();
            }
            filters.putIfAbsent("layers", "18");
        }
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }
}
