package org.acme.getting.started;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.translate.TranslateException;
import ai.djl.quarkus.runtime.DjlPredictorProducer;
import java.io.IOException;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Simple DJL example that just runs prediction when you visit the page.
 */
@Path("/")
public class DjlResource {

    @Inject
    DjlPredictorProducer predictorProducer;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws TranslateException, IOException {
        // Use sample image
        java.nio.file.Path imageFile = Paths.get("/Volumes/Unix/projects/Joule/examples/src/test/resources/kitten.jpg");
        Image img = ImageFactory.getInstance().fromFile(imageFile);

        // Get predictor
        Predictor<Image, Classifications> predictor = (Predictor<Image, Classifications>) predictorProducer.predictor();

        Classifications prediction = predictor.predict(img);
        return prediction.toString();
    }
}
