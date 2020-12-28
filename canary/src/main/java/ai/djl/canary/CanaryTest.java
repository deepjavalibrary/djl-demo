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
package ai.djl.canary;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.ModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.onnxruntime.zoo.tabular.randomforest.IrisFlower;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.util.cuda.CudaUtils;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Canary test for DJL. */
@SuppressWarnings("MissingJavadocMethod")
public final class CanaryTest {

    private static final Logger logger = LoggerFactory.getLogger(CanaryTest.class);

    private CanaryTest() {}

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        logger.info("");
        logger.info("----------Environment Variables----------");
        System.getenv().forEach((k, v) -> logger.info(k + ": " + v));

        logger.info("");
        logger.info("----------Default Engine----------");
        Engine.debugEnvironment();

        logger.info("");
        logger.info("----------Device information----------");
        int gpuCount = CudaUtils.getGpuCount();
        int deviceCount = Device.getGpuCount();
        logger.info("GPU Count: {}", gpuCount);
        if (gpuCount > 0) {
            logger.info("CUDA: {}", CudaUtils.getCudaVersionString());
            logger.info("ARCH: {}", CudaUtils.getComputeCapability(0));
        }
        logger.info("Engine visible GPU: {}", deviceCount);
        logger.info("Default Device: {}", Device.defaultDevice());

        String djlEngine = System.getenv("DJL_ENGINE");
        if (djlEngine == null) {
            djlEngine = "mxnet-native-auto";
        }

        if (djlEngine.contains("-native-cu") && deviceCount == 0) {
            throw new AssertionError("Expecting load engine on GPU.");
        } else if (djlEngine.startsWith("onnxruntime")) {
            testOnnxRuntime();
            return;
        } else if (djlEngine.startsWith("dlr")) {
            testDlr();
            return;
        }

        logger.info("");
        logger.info("----------Test inference----------");
        String url = "https://resources.djl.ai/images/dog_bike_car.jpg";
        Image img = ImageFactory.getInstance().fromUrl(url);
        String backbone = "resnet50";
        Map<String, String> options = null;
        if ("TensorFlow".equals(Engine.getInstance().getEngineName())) {
            backbone = "mobilenet_v2";
            options = new ConcurrentHashMap<>();
            options.put("Tags", "");
        }

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optFilter("backbone", backbone)
                        .optOptions(options)
                        .build();

        try (ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria)) {
            try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
                DetectedObjects detection = predictor.predict(img);
                logger.info("{}", detection);
            }
        }
    }

    private static void testOnnxRuntime() throws ModelException, IOException, TranslateException {
        Criteria<IrisFlower, Classifications> criteria =
                Criteria.builder()
                        .setTypes(IrisFlower.class, Classifications.class)
                        .optEngine("OnnxRuntime") // use OnnxRuntime engine
                        .build();

        IrisFlower virginica = new IrisFlower(1.0f, 2.0f, 3.0f, 4.0f);
        try (ZooModel<IrisFlower, Classifications> model = ModelZoo.loadModel(criteria);
                Predictor<IrisFlower, Classifications> predictor = model.newPredictor()) {
            Classifications classifications = predictor.predict(virginica);
            logger.info("{}", classifications);
        }
    }

    private static void testDlr() throws ModelException, IOException, TranslateException {
        String os;
        if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
            os = "osx";
        } else if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            os = "linux";
        } else {
            throw new AssertionError("DLR only work on mac and Linux.");
        }
        ImageClassificationTranslator translator =
                ImageClassificationTranslator.builder()
                        .addTransform(new Resize(224, 224))
                        .addTransform(new ToTensor())
                        .build();
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                        .optFilter("layers", "50")
                        .optFilter("os", os)
                        .optTranslator(translator)
                        .optEngine("DLR")
                        .optProgress(new ProgressBar())
                        .build();
        String url = "https://resources.djl.ai/images/kitten.jpg";
        Image image = ImageFactory.getInstance().fromUrl(url);
        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
                Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Classifications classifications = predictor.predict(image);
            logger.info("{}", classifications);
        }
    }
}
