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
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JOptionPane;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class WebCam {

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        ZooModel<Image, DetectedObjects> model = loadModel();
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();

        OpenCV.loadShared();
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("No camera detected");
            return;
        }

        Mat image = new Mat();
        boolean captured = false;
        for (int i = 0; i < 10; ++i) {
            captured = capture.read(image);
            if (captured) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignore) {
                // ignore
            }
        }
        if (!captured) {
            JOptionPane.showConfirmDialog(null, "Failed to capture image from WebCam.");
        }

        ViewerFrame frame = new ViewerFrame(image.width(), image.height());
        ImageFactory factory = ImageFactory.getInstance();

        while (capture.isOpened()) {
            if (!capture.read(image)) {
                break;
            }
            Image img = factory.fromImage(toBufferedImage(image));
            DetectedObjects detections = predictor.predict(img);
            img.drawBoundingBoxes(detections);

            frame.showImage((BufferedImage) img.getWrappedImage());
        }

        capture.release();

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

        return criteria.loadModel();
    }

    private static BufferedImage toBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int type =
                mat.channels() != 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;

        if (type == BufferedImage.TYPE_3BYTE_BGR) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        }

        byte[] data = new byte[width * height * (int) mat.elemSize()];
        mat.get(0, 0, data);

        BufferedImage ret = new BufferedImage(width, height, type);
        ret.getRaster().setDataElements(0, 0, width, height, data);

        return ret;
    }
}
