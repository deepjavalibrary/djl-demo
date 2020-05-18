package com.examples;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import javax.annotation.PostConstruct;
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
import ai.djl.repository.zoo.ZooProvider;
import ai.djl.tensorflow.engine.LibUtils;
import ai.djl.tensorflow.engine.TfEngine;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Platform;

@ApplicationScoped
public class ExampleService {

    private Predictor<BufferedImage, Classifications> predictor;

    public ExampleService() throws Exception {

        System.out.println("Library Path: " + System.getProperty("org.bytedeco.javacpp.platform.preloadpath"));

        // Path to the TensorFlow Model
        System.out.println("ai.djl.repository.zoo.location=" + System.getProperty("ai.djl.repository.zoo.location"));

        URL url = Thread.currentThread().getContextClassLoader().getResource("native/lib/tensorflow.properties");
        System.out.println("URL: " + url.toURI().toString());
        Platform platform = Platform.fromUrl(url);
        System.out.println("Platform: " + platform.getLibraries() + " " + platform.getVersion());

        LibUtils.loadLibrary();
        // See if TF loaded correctly or not. If not, expect
        // java.lang.UnsatisfiedLinkError
        // TfEngine.getInstance().debugEnvironment();

    }

    public Classifications predict() throws MalformedURLException, IOException, ModelNotFoundException,
            MalformedModelException, TranslateException {

        // loadLibrary();

        System.out.println(ModelZoo.listModels().values().toString());

        ServiceLoader<ZooProvider> providers = ServiceLoader.load(ZooProvider.class);
        for (ZooProvider provider : providers) {
            System.out.println(provider.getName());
        }

        Criteria<BufferedImage, Classifications> criteria = Criteria.builder()
                .setTypes(BufferedImage.class, Classifications.class).optTranslator(new MyTranslator())
                .optProgress(new ProgressBar()).build();

        ZooModel<BufferedImage, Classifications> model = ModelZoo.loadModel(criteria);
        this.predictor = model.newPredictor();

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