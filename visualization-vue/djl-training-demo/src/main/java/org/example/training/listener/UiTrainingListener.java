/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package org.example.training.listener;

import ai.djl.training.Trainer;
import ai.djl.training.listener.TrainingListener;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.example.training.verticle.DataVerticle;
import org.example.training.verticle.WebVerticle;

public class UiTrainingListener  implements TrainingListener {

    private static final Logger logger = LoggerFactory.getLogger(UiTrainingListener.class.getCanonicalName());

    private Vertx vertx;
    private DataVerticle dataVerticle;

    public UiTrainingListener() {
        logger.info("UiTrainingListener starting...");
        vertx = Vertx.vertx();
        dataVerticle = new DataVerticle();
        vertx.deployVerticle(dataVerticle);
        vertx.deployVerticle(new WebVerticle());
    }

    @Override
    public void onEpoch(Trainer trainer) {
        dataVerticle.setEpoch(trainer);
    }

    @Override
    public void onTrainingBatch(Trainer trainer, BatchData batchData) {
        dataVerticle.setTrainingBatch(trainer, batchData);
    }

    @Override
    public void onValidationBatch(Trainer trainer, BatchData batchData) {
        dataVerticle.setValidationBatch(trainer, batchData);
    }

    @Override
    public void onTrainingBegin(Trainer trainer) {
        logger.info("onTrainingBegin ...");
    }

    @Override
    public void onTrainingEnd(Trainer trainer) {
        logger.info("onTrainingEnd ...");
        vertx.close();
    }
}
