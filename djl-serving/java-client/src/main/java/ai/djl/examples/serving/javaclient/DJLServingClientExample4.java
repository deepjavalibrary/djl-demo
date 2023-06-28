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
package ai.djl.examples.serving.javaclient;

import ai.djl.engine.Engine;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;

import java.util.Map;

public class DJLServingClientExample4 {

    public static void main(String[] args) throws Exception {
        // Register model with tensor in/tensor out mode
String url =
    "https://resources.djl.ai/demo/pytorch/traced_resnet18.zip?translatorFactory=ai.djl.translate.NoopServingTranslatorFactory";
Map<String, String> params = Map.of("url", url, "engine", "PyTorch");
HttpUtils.postRequest("http://localhost:8080/models", params, null, null, null);

try (NDManager manager = NDManager.newBaseManager()) {
Engine engine = manager.getEngine();
NDList list;
if ("PyTorch".equals(engine.getEngineName())) {
    // You need include a proper engine in the build.gradle to perform the following
    // NDArray operations:
    ImageFactory factory = ImageFactory.getInstance();
    Image image = factory.fromUrl("https://resources.djl.ai/images/kitten.jpg");
    NDArray array = image.toNDArray(manager);
    array = new Resize(224, 224).transform(array);
    array = new ToTensor().transform(array);
    array = array.expandDims(0);
    list = new NDList(array);
} else {
    // create a fake NDArray input for demo
    NDArray array = manager.ones(new Shape(1, 3, 224, 224));
    list = new NDList(array);
}

// Run inference
byte[] data = list.encode();
byte[] response =
        HttpUtils.postRequest(
                "http://localhost:8080/predictions/traced_resnet18",
                null,
                "tenosr/ndlist",
                data,
                null);
NDList output = NDList.decode(manager, response);
System.out.println(output.get(0));
        }

        // unregister model
        HttpUtils.unregisterModel("http://localhost:8080/models/traced_resnet18");
    }
}
