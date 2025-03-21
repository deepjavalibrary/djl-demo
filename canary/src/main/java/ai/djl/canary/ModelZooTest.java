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
package ai.djl.canary;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.ndarray.NDList;
import ai.djl.repository.Artifact;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelLoader;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ModelZooTest {

    private static final Logger logger = LoggerFactory.getLogger(ModelZooTest.class);

    public static void main(String[] args) throws ModelException, IOException {
        String djlEngine = System.getenv("DJL_ENGINE");
        String groupId;
        if (djlEngine == null) {
            groupId = "ai.djl.zoo";
        } else if (djlEngine.startsWith("pytorch")) {
            groupId = "ai.djl.pytorch";
        } else if (djlEngine.startsWith("mxnet")) {
            groupId = "ai.djl.mxnet";
        } else if (djlEngine.startsWith("tensorflow")) {
            groupId = "ai.djl.tensorflow";
        } else if (djlEngine.startsWith("onnxruntime")) {
            groupId = "ai.djl.onnxruntime";
        } else if (djlEngine.startsWith("tokenizer")) {
            groupId = "ai.djl.huggingface.pytorch";
        } else {
            throw new AssertionError("Unsupported model zoo: " + djlEngine);
        }

        System.setProperty("DJL_CACHE_DIR", "build/cache");
        String userHome = System.getProperty("user.home");
        System.setProperty("ENGINE_CACHE_DIR", userHome + "/.djl.ai");

        boolean download = Boolean.parseBoolean(Utils.getEnvOrSystemProperty("DOWNLOAD"));
        testModelZoo(groupId, download);
    }

    public static void testModelZoo(String groupId, boolean download)
            throws IOException, ModelException {
        logger.info("====== Testing Model zoo: {} ======", groupId);
        ModelZoo zoo = ModelZoo.getModelZoo(groupId);
        if (zoo == null) {
            throw new AssertionError("Model zoo not found: " + groupId);
        }
        for (ModelLoader modelLoader : zoo.getModelLoaders()) {
            logger.info("+--- {}: {}", modelLoader.getApplication(), modelLoader.getArtifactId());
            List<Artifact> artifacts = modelLoader.getMrl().listArtifacts();
            for (Artifact artifact : artifacts) {
                logger.info("|    +--- {}", artifact.getName());
                Criteria<NDList, NDList> criteria =
                        Criteria.builder()
                                .setTypes(NDList.class, NDList.class)
                                .optFilters(artifact.getProperties())
                                .build();
                if (download) {
                    Model model = modelLoader.loadModel(criteria);
                    model.close();
                    Path path = model.getModelPath();
                    Utils.deleteQuietly(path);
                }
            }
        }
    }
}
