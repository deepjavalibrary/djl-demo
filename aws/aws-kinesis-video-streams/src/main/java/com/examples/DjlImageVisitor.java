package com.examples;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications.Classification;
import ai.djl.modality.cv.ImageVisualization;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.mxnet.zoo.MxModelZoo;
import ai.djl.repository.zoo.ModelNotFoundException;
import com.amazonaws.kinesisvideo.parser.mkv.Frame;
import com.amazonaws.kinesisvideo.parser.mkv.FrameProcessException;
import com.amazonaws.kinesisvideo.parser.utilities.FragmentMetadata;
import com.amazonaws.kinesisvideo.parser.utilities.H264FrameDecoder;
import com.amazonaws.kinesisvideo.parser.utilities.MkvTrackMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class DjlImageVisitor extends H264FrameDecoder {

    Predictor<BufferedImage, DetectedObjects> predictor;
    int counter;

    public DjlImageVisitor() throws IOException, ModelNotFoundException, MalformedModelException {
        predictor = MxModelZoo.SSD.loadModel().newPredictor();
        counter = 0;
    }

    @Override
    public void process(
            Frame frame,
            MkvTrackMetadata trackMetadata,
            Optional<FragmentMetadata> fragmentMetadata)
            throws FrameProcessException {

        try {
            BufferedImage bufferedImage = decodeH264Frame(frame, trackMetadata);
            DetectedObjects prediction = predictor.predict(bufferedImage);
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

            ImageVisualization.drawBoundingBoxes(bufferedImage, prediction);
            File outputFile = Paths.get("out/image-" + counter + ".png").toFile();
            ImageIO.write(bufferedImage, "png", outputFile);
            counter++;
        } catch (Exception e) {
            throw new FrameProcessException("Failed to predict", e);
        }
    }
}
