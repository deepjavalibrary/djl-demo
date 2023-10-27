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
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.BytesSupplier;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.serving.workflow.Workflow;
import ai.djl.serving.workflow.WorkflowExpression;
import ai.djl.serving.workflow.function.WorkflowFunction;
import ai.djl.translate.Pipeline;
import ai.djl.translate.StackBatchifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Workflow function "preprocess" accepts an input image, then returns the preprocessed array. */
public class PreprocessWF extends WorkflowFunction {

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<WorkflowExpression.Item> run(
            Workflow.WorkflowExecutor executor, List<Workflow.WorkflowArgument> args) {
        if (args.size() != 1) {
            throw new IllegalArgumentException("Expected one argument to preprocess");
        }
        return evaluateArgs(args)
                .thenApply(
                        pa -> {
                            try {
                                Output output = new Output();
                                BytesSupplier data = pa.get(0).getInput().getData();

                                if (data == null) {
                                    throw new RuntimeException("Input data is empty");
                                }
                                Image image =
                                        ImageFactory.getInstance()
                                                .fromInputStream(
                                                        new ByteArrayInputStream(
                                                                data.getAsBytes()));

                                try (NDManager manager = NDManager.newBaseManager()) {
                                    // Transformation
                                    NDArray array = image.toNDArray(manager);
                                    Pipeline pipeline = new Pipeline();
                                    pipeline.add(new CenterCrop());
                                    pipeline.add(new Resize(224, 224));
                                    pipeline.add(new ToTensor());
                                    NDList list = pipeline.transform(new NDList(array));

                                    // Stack the input because the model expects batched input
                                    StackBatchifier batchifier = new StackBatchifier();
                                    NDList batched = batchifier.batchify(new NDList[] {list});

                                    // Add to output
                                    output.addProperty("Content-Type", "tensor/npz");
                                    output.add(batched.encode(NDList.Encoding.NPZ));

                                    // Attach the NDList resource to NDManager to avoid memory leak
                                    manager.tempAttachAll(batched);
                                }
                                return new WorkflowExpression.Item(output);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }
}
