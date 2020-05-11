package com.examples;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.util.BufferedImageUtils;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

@ApplicationScoped
public class ExampleService {

    private Predictor<BufferedImage, Classifications> predictor;

    public ExampleService() throws Exception {
        Criteria<BufferedImage, Classifications> criteria = Criteria.builder()
                .setTypes(BufferedImage.class, Classifications.class).optTranslator(new MyTranslator()).build();

        ZooModel<BufferedImage, Classifications> model = ModelZoo.loadModel(criteria);
        this.predictor = model.newPredictor();
    }

    public Classifications predict() throws MalformedURLException, IOException, ModelNotFoundException,
            MalformedModelException, TranslateException {

        String imagePath = "https://github.com/ieee8023/covid-chestxray-dataset/blob/master/images/01E392EE-69F9-4E33-BFCE-E5C968654078.jpeg?raw=true";
        BufferedImage image = BufferedImageUtils.fromUrl(new URL(imagePath));

        return predictor.predict(image);
    }

    private static final class MyTranslator implements Translator<BufferedImage, Classifications> {
        private static final List<String> CLASSES = Arrays.asList("covid-19", "normal");

        @Override
        public NDList processInput(TranslatorContext ctx, BufferedImage input) {
            NDArray array = BufferedImageUtils.toNDArray(ctx.getNDManager(), input, NDImageUtils.Flag.COLOR);
            array = NDImageUtils.resize(array, 224).div(255.0f);
            return new NDList(array);
        }

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) {
            NDArray probabilities = list.singletonOrThrow();
            return new Classifications(CLASSES, probabilities);
        }
    }
}