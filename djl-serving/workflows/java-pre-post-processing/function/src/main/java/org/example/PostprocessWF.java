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

import ai.djl.modality.Classifications;
import ai.djl.modality.Output;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.serving.workflow.Workflow;
import ai.djl.serving.workflow.WorkflowExpression;
import ai.djl.serving.workflow.function.WorkflowFunction;
import ai.djl.translate.StackBatchifier;
import ai.djl.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Workflow function "postprocess" accepts the probabilities array, then returns the classifications
 * as json.
 */
public class PostprocessWF extends WorkflowFunction {

    private List<String> classes;

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<WorkflowExpression.Item> run(
            Workflow.WorkflowExecutor executor, List<Workflow.WorkflowArgument> args) {
        if (args.size() != 1) {
            throw new IllegalArgumentException("Expected one argument to postprocess");
        }

        if (classes == null) {
            try (InputStream in = getClass().getResourceAsStream("/synset.txt")) {
                if (in == null) {
                    throw new RuntimeException("Not able to read synset.txt file");
                }
                classes = Utils.readLines(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return evaluateArgs(args)
                .thenApply(
                        pa -> {
                            Output output = new Output();
                            try (NDManager manager = NDManager.newBaseManager()) {
                                NDList list = pa.get(0).getInput().getDataAsNDList(manager);
                                if (list == null) {
                                    throw new RuntimeException("Input data is empty");
                                }

                                // Split the combined batch into individual NDArrays
                                StackBatchifier batchifier = new StackBatchifier();
                                NDList[] unbatched = batchifier.unbatchify(list);
                                NDArray probabilitiesNd = unbatched[0].singletonOrThrow();

                                // Apply softmax
                                probabilitiesNd = probabilitiesNd.softmax(0);
                                Classifications classifications =
                                        new Classifications(classes, probabilitiesNd);

                                // Add to output
                                output.setCode(200);
                                output.setMessage("OK");
                                output.add(classifications.toJson());
                            }
                            return new WorkflowExpression.Item(output);
                        });
    }
}
