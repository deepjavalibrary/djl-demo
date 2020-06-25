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
package ai.djl.examples.doodle;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.util.Utils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class Handler implements RequestStreamHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        // DJL saves model and native libraries in cache folder.
        // In AWS-Lambda only /tmp folder is writable.
        System.setProperty("DJL_CACHE_DIR", "/tmp/djl_cache");
    }

    @Override
    public void handleRequest(InputStream is, OutputStream os, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        String input = Utils.toString(is);
        try {
            Request request = GSON.fromJson(input, Request.class);
            String base64Img = request.getImageData().split(",")[1];
            byte[] imgBytes = Base64.getDecoder().decode(base64Img);
            Image img;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes)) {
                ImageFactory factory = ImageFactory.getInstance();
                img = factory.fromInputStream(bis);
            }

            Translator<Image, Classifications> translator =
                    ImageClassificationTranslator.builder()
                            .addTransform(new ToTensor())
                            .optFlag(Image.Flag.GRAYSCALE)
                            .optApplySoftmax(true)
                            .build();
            Criteria<Image, Classifications> criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optModelUrls(
                                    "https://alpha-djl-demos.s3.amazonaws.com/model/quickdraw/doodle_mobilenet.zip")
                            .optTranslator(translator)
                            .build();
            ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
            try (Predictor<Image, Classifications> predictor = model.newPredictor()) {
                List<Classifications.Classification> result = predictor.predict(img).topK(5);
                os.write(GSON.toJson(result).getBytes(StandardCharsets.UTF_8));
            }
        } catch (RuntimeException | ModelException | TranslateException e) {
            logger.log("Failed handle input: " + input);
            logger.log(e.toString());
            String msg = "{\"status\": \"invoke failed: " + e.toString() + "\"}";
            os.write(msg.getBytes(StandardCharsets.UTF_8));
        }
    }
}
