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
import ai.djl.modality.cv.DetectedObjects;
import ai.djl.modality.cv.ImageVisualization;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_java;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class WebCam {

    static {
        Loader.load(opencv_java.class);
    }

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("No camera detected");
            return;
        }

        ZooModel<BufferedImage, DetectedObjects> model = loadModel();
        Predictor<BufferedImage, DetectedObjects> predictor = model.newPredictor();

        Mat image = new Mat();
        capture.read(image);

        ViewerFrame frame = new ViewerFrame(image.width(), image.height());

        while (capture.isOpened()) {
            capture.read(image);
            BufferedImage img = toBufferedImage(image);
            DetectedObjects detections = predictor.predict(img);
            drawBoxImage(img, detections);

            frame.showImage(img);
        }

        capture.release();

        predictor.close();
        model.close();

        System.exit(0);
    }

    private static ZooModel<BufferedImage, DetectedObjects> loadModel()
            throws IOException, ModelException {
        Criteria<BufferedImage, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(BufferedImage.class, DetectedObjects.class)
                        .optFilter("backbone", "mobilenet1.0")
                        .optFilter("dataset", "voc")
                        .optProgress(new ProgressBar())
                        .build();

        return ModelZoo.loadModel(criteria);
    }

    private static void drawBoxImage(BufferedImage img, DetectedObjects detection) {
        // Make image copy with alpha channel because original image was jpg
        Graphics2D g = img.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        ImageVisualization.drawBoundingBoxes(img, detection);
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
