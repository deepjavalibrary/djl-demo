package org.acme.getting.started;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.quarkus.runtime.DjlPredictorProducer;
import ai.djl.translate.Translator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

/**
 * Simple DJL example that just runs prediction when you visit the page.
 */
@Path("/")
public class DjlResource {

    private static final String IMAGE_URL = "https://djl-ai.s3.amazonaws.com/resources/images/kitten_small.jpg";
    private static final float[] MEAN = {103.939f, 116.779f, 123.68f};
    private static final float[] STD = {1f, 1f, 1f};

    private Translator<Image, Classifications> translator;

    @Inject
    DjlPredictorProducer predictorProducer;

    public DjlResource() {
        translator = ImageClassificationTranslator.builder()
            .addTransform(new Resize(224))
            .addTransform(new Normalize(MEAN, STD))
            .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws TranslateException, IOException, ImageReadException {
        // Use sample image
        URL url = new URL(IMAGE_URL);
        Image image;
        try (InputStream is = url.openStream()) {
            image = ImageFactory.getInstance().fromImage(Imaging.getBufferedImage(is));
        }

        // Get predictor
        Predictor<Image, Classifications> predictor = predictorProducer.model().newPredictor(translator);

        Classifications prediction = predictor.predict(image);
        return prediction.toString() + "\n";
    }
}
