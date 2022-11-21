/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package ai.djl.examples.serving.javaclient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DJLServingClientExample1 {

    public static void main(String[] args) throws Exception {
        // Register model
        Map<String, String> params = new HashMap<>();
        params.put("url", "https://resources.djl.ai/demo/pytorch/traced_resnet18.zip");
        params.put("engine", "PyTorch");
        HttpUtils.postRequest("http://localhost:8080/models", params, null, null, null);

        // Run inference
        String response = HttpUtils.postRequest("http://localhost:8080/predictions/traced_resnet18", null, "application/octet-stream", null, new File("/tmp/kitten.jpg"));
        System.out.println(response);
    }
}
