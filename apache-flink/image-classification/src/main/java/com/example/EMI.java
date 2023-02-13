package com.example;

import ai.djl.ModelException;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.translate.TranslateException;

import com.example.dataset.S3ImageDataset;
import com.example.ml.Classifier;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.List;

public class EMI {

    private static final Logger logger = LoggerFactory.getLogger(EMI.class);

    public static void main(String[] args) throws Exception {
        MultipleParameterTool params = MultipleParameterTool.fromArgs(args);
        // set up the execution environment
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        // make parameters available in the web interface
        env.getConfig().setGlobalJobParameters(params);

        int batchSize = 2;

        // customize your s3client with credentials if needed
        S3Client s3 =
                S3Client.builder()
                        .credentialsProvider(AnonymousCredentialsProvider.create())
                        .build();
        String bucket = "djl-ai";
        String prefix = "resources/demo/spark/image_classification/";
        S3ImageDataset s3Images = S3ImageDataset.getInstance(bucket, prefix, s3);

        // list images in batch
        List<List<Image>> images = s3Images.listImages(batchSize);
        logger.info("Images in dataset: {}", images.size());

        DataSet<List<Image>> dataSet = env.fromCollection(images);
        Classifier classifier = Classifier.getInstance();
        InferenceDelegate delegate = new InferenceDelegate(classifier);
        dataSet.flatMap(delegate).writeAsText("asd.txt", FileSystem.WriteMode.OVERWRITE);
        env.execute("Embedded Model Inference");
        delegate.shutdown();

        logger.info("Results saved in asd.txt file.");
    }

    public static final class InferenceDelegate implements FlatMapFunction<List<Image>, String> {

        private Classifier classifier;

        public InferenceDelegate(Classifier classifier) {
            this.classifier = classifier;
        }

        @Override
        public void flatMap(List<Image> images, Collector<String> out) {
            try {
                List<Classifications> list = classifier.predict(images);
                for (Classifications classifications : list) {
                    Classifications.Classification cl = classifications.best();
                    String ret = cl.getClassName() + ": " + cl.getProbability();
                    out.collect(ret);
                }
            } catch (ModelException | IOException | TranslateException e) {
                logger.error("Failed predict", e);
            }
        }

        public void shutdown() {
            classifier.close();
        }
    }
}
