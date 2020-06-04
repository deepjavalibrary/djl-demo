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

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Joints;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MultiEngine {

    private static final Logger logger = LoggerFactory.getLogger(MultiEngine.class);

    private MultiEngine() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        Path imageFile = Paths.get("src/test/resources/pose_soccer.png");
        Image img = ImageFactory.getInstance().fromFile(imageFile);

        // First detect a person from the main image using PyTorch engine
        Image person = detectPersonWithPyTorchModel(img);

        // If no person is detected, we can't pass to next model, log and exit.
        if (person == null) {
            logger.warn("No person found in image.");
            return;
        }

        // There was a person found, we pass the image to our MxNet Model.
        Joints joints = detectJointsWithMxnetModel(person);

        // Outputs the resulting image with the joints to build/output
        saveJointsImage(person, joints);
    }

    private static Image detectPersonWithPyTorchModel(Image img)
            throws MalformedModelException, ModelNotFoundException, IOException,
                    TranslateException {

        // Criteria object to load the model from model zoo
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optProgress(new ProgressBar())
                        // We specify a resnet50 model that runs using the PyTorch engine here.
                        .optFilter("size", "300")
                        .optFilter("backbone", "resnet50")
                        .optFilter("dataset", "coco")
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();

        // Inference call to detect the person form the image.
        DetectedObjects detectedObjects;
        try (ZooModel<Image, DetectedObjects> ssd = ModelZoo.loadModel(criteria);
                Predictor<Image, DetectedObjects> predictor = ssd.newPredictor()) {
            detectedObjects = predictor.predict(img);
        }

        // Get the first resulting image of the person and return it
        List<DetectedObjects.DetectedObject> items = detectedObjects.items();
        for (DetectedObjects.DetectedObject item : items) {
            if ("person".equals(item.getClassName())) {
                Rectangle rect = item.getBoundingBox().getBounds();
                int width = img.getWidth();
                int height = img.getHeight();
                return img.getSubimage(
                        (int) (rect.getX() * width),
                        (int) (rect.getY() * height),
                        (int) (rect.getWidth() * width),
                        (int) (rect.getHeight() * height));
            }
        }
        return null;
    }

    private static Joints detectJointsWithMxnetModel(Image person)
            throws MalformedModelException, ModelNotFoundException, IOException,
                    TranslateException {

        // Criteria object to load the model from model zoo
        Criteria<Image, Joints> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.POSE_ESTIMATION)
                        .setTypes(Image.class, Joints.class)
                        // We specify a resnet18_v1b model that runs using MXNet engine here.
                        .optFilter("backbone", "resnet18")
                        .optFilter("flavor", "v1b")
                        .optFilter("dataset", "imagenet")
                        .optEngine("MXNet") // Use MXNet engine
                        .build();

        // Run inference on the image of a person and detect the joints
        try (ZooModel<Image, Joints> pose = ModelZoo.loadModel(criteria);
                Predictor<Image, Joints> predictor = pose.newPredictor()) {
            return predictor.predict(person);
        }
    }

    private static void saveJointsImage(Image img, Joints joints) throws IOException {
        Path outputDir = Paths.get("build/output");
        Files.createDirectories(outputDir);

        img.drawJoints(joints);

        Path imagePath = outputDir.resolve("joints.png");
        // Must use png format because you can't save as jpg with an alpha channel
        img.save(Files.newOutputStream(imagePath), "png");
        logger.info("Pose image has been saved in: {}", imagePath);
    }
}
