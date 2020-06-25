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
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import com.github.sarxos.webcam.Webcam;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;

public class WebCam {

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        ZooModel<Image, DetectedObjects> model = loadModel();
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();

        List<Webcam> webcams = Webcam.getWebcams();

        Optional<Webcam> optionalWebcam =
                webcams.stream()
                        // Ignore any virtual cameras for now
                        .filter(webcam -> !webcam.getName().toLowerCase().contains("virtual"))
                        // Ignore any cameras that fail to open
                        .filter(Webcam::open)
                        // Pick the first camera
                        .findFirst();

        if (!optionalWebcam.isPresent()) {
            System.out.println("No camera detected");
            return;
        }

        Webcam webcam = optionalWebcam.get();
        adjustViewSize(webcam);

        if (!webcam.isOpen()) {
            System.out.println("Camera is not open");
            return;
        }
        BufferedImage bufferedImage = webcam.getImage();

        if (bufferedImage == null) {
            JOptionPane.showConfirmDialog(null, "Failed to capture image from WebCam.");
            return;
        }

        ViewerFrame frame = new ViewerFrame(bufferedImage.getWidth(), bufferedImage.getHeight());

        ImageFactory factory = ImageFactory.getInstance();

        while (webcam.isOpen()) {
            bufferedImage = webcam.getImage();
            Image img = factory.fromImage(bufferedImage);
            DetectedObjects detections = predictor.predict(img);
            img.drawBoundingBoxes(detections);

            frame.showImage((BufferedImage) img.getWrappedImage());
        }

        webcam.close();

        predictor.close();
        model.close();

        System.exit(0);
    }

    private static ZooModel<Image, DetectedObjects> loadModel() throws IOException, ModelException {
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optFilter("backbone", "mobilenet1.0")
                        .optFilter("dataset", "voc")
                        .optProgress(new ProgressBar())
                        .build();

        return ModelZoo.loadModel(criteria);
    }

    private static void adjustViewSize(Webcam webcam) {
        Dimension dimension = webcam.getViewSize();
        double width = (int) dimension.getWidth();
        double height = (int) dimension.getHeight();
        if (width < 512) {
            int newHeight = (int) (height * 512 / width);
            dimension = new Dimension(512, newHeight);
            webcam.close();
            try {
                webcam.setCustomViewSizes(dimension);
                webcam.setViewSize(dimension);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            webcam.open();
        }
    }
}
