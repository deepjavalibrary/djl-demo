package com.example.ml;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.metric.Metrics;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class Classifier implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Classifier.class);

    private static Classifier instance;

    private String modelUrl;

    private transient ZooModel<Image, Classifications> model;
    private transient Predictor<Image, Classifications> predictor;

    private Classifier(String modelUrl) {
        this.modelUrl = modelUrl;
    }

    public static Classifier getInstance() {
        return getInstance(null);
    }

    public static synchronized Classifier getInstance(String modelUrl) {
        if (instance == null) {
            if (modelUrl == null) {
                modelUrl = "djl://ai.djl.pytorch/resnet";
                logger.info("Use resnet18 from DJL model zoo.");
            } else {
                logger.info("Loading model from: {}", modelUrl);
            }
            instance = new Classifier(modelUrl);
        }
        return instance;
    }

    public List<Classifications> predict(List<Image> images)
            throws ModelException, IOException, TranslateException {
        return getPredictor().batchPredict(images);
    }

    public synchronized void close() {
        if (predictor != null) {
            predictor.close();
            model.close();
            predictor = null;
            model = null;
        }
    }

    private Predictor<Image, Classifications> getPredictor() throws ModelException, IOException {
        if (predictor == null) {
            Criteria<Image, Classifications> criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optModelUrls(modelUrl)
                            .build();
            model = criteria.loadModel();
            predictor = model.newPredictor();
            Metrics metrics = new Metrics();
            metrics.setLimit(100); // print metrics every 100 inference
            metrics.setOnLimit((m, k) -> logger.info("{}", m.percentile(k, 50)));
        }
        return predictor;
    }
}
