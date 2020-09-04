package com.examples;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications.Classification;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import com.amazonaws.kinesisvideo.parser.mkv.Frame;
import com.amazonaws.kinesisvideo.parser.mkv.FrameProcessException;
import com.amazonaws.kinesisvideo.parser.utilities.FragmentMetadata;
import com.amazonaws.kinesisvideo.parser.utilities.H264FrameDecoder;
import com.amazonaws.kinesisvideo.parser.utilities.MkvTrackMetadata;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

public class DjlImageVisitor extends H264FrameDecoder {

    Predictor<Image, DetectedObjects> predictor;
    int counter;
    private ImageFactory factory;

    public DjlImageVisitor() throws IOException, ModelException {
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optArtifactId("ai.djl.mxnet:ssd")
                        .build();
        predictor = ModelZoo.loadModel(criteria).newPredictor();
        counter = 0;
        factory = ImageFactory.getInstance();
    }

    @Override
    public void process(
            Frame frame,
            MkvTrackMetadata trackMetadata,
            Optional<FragmentMetadata> fragmentMetadata)
            throws FrameProcessException {

        try {
            Image image = factory.fromImage(decodeH264Frame(frame, trackMetadata));
            DetectedObjects prediction = predictor.predict(image);
            String classStr =
                    prediction
                            .items()
                            .stream()
                            .map(Classification::getClassName)
                            .collect(Collectors.joining(", "));
            System.out.println("Found objects: " + classStr);
            boolean hasPerson =
                    prediction
                            .items()
                            .stream()
                            .anyMatch(
                                    c ->
                                            "person".equals(c.getClassName())
                                                    && c.getProbability() > 0.5);

            image.drawBoundingBoxes(prediction);
            Path outputFile = Paths.get("out/image-" + counter + ".png");
            try (OutputStream os = Files.newOutputStream(outputFile)) {
                image.save(os, "png");
            }
            counter++;
        } catch (Exception e) {
            throw new FrameProcessException("Failed to predict", e);
        }
    }
}
