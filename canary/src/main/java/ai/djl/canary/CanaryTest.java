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
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.sentencepiece.SpTokenizer;
import ai.djl.training.util.DownloadUtils;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.util.cuda.CudaUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        logger.info("GPU Count: {}", gpuCount);
        if (gpuCount > 0) {
            logger.info("CUDA: {}", CudaUtils.getCudaVersionString());
            logger.info("ARCH: {}", CudaUtils.getComputeCapability(0));
        }

        String djlEngine = System.getenv("DJL_ENGINE");
        if (djlEngine == null) {
            djlEngine = "mxnet-native-auto";
        }

        Device device = NDManager.newBaseManager().getDevice();
        if (djlEngine.contains("-native-cu") && !device.isGpu()) {
            throw new AssertionError("Expecting load engine on GPU.");
        } else if (djlEngine.startsWith("tensorrt")) {
            testTensorrt();
            return;
        } else if (djlEngine.startsWith("onnxruntime")) {
            testOnnxRuntime();
            return;
        } else if (djlEngine.startsWith("xgboost")) {
            testXgboost();
            return;
        } else if (djlEngine.startsWith("tflite")) {
            testTflite();
            return;
        } else if (djlEngine.startsWith("python")) {
            testPython();
            return;
        } else if (djlEngine.startsWith("dlr")) {
            testDlr();

            // similar to DLR, fastText and SentencePiece only support Mac and Ubuntu 16.04+
            testFastText();
            testSentencePiece();
            return;
        } else if (djlEngine.startsWith("paddle")) {
            testPaddle();
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

    private static void testTensorrt() throws ModelException, IOException, TranslateException {
        if (!System.getProperty("os.name").startsWith("Linux") || !CudaUtils.hasCuda()) {
            throw new AssertionError("TensorRT only work on Linux GPU instance.");
        }
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optEngine("TensorRT") // use TensorRT engine
                        .optModelUrls(
                                "https://mlrepo.djl.ai/model/cv/image_classification/ai/djl/onnxruntime/resnet/0.0.1/resnet18_v1-7.tar.gz")
                        .build();

        String url = "https://resources.djl.ai/images/kitten.jpg";
        Image image = ImageFactory.getInstance().fromUrl(url);
        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
                Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Classifications classifications = predictor.predict(image);
            logger.info("{}", classifications);
        }
    }

    private static void testOnnxRuntime() throws ModelException, IOException, TranslateException {
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optEngine("OnnxRuntime") // use OnnxRuntime engine
                        .optModelUrls("djl://ai.djl.onnxruntime/resnet/0.0.1/resnet18_v1-7")
                        .build();

        String url = "https://resources.djl.ai/images/kitten.jpg";
        Image image = ImageFactory.getInstance().fromUrl(url);
        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
                Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Classifications classifications = predictor.predict(image);
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

    public static void testFastText() throws IOException, ModelException, TranslateException {
        if (System.getProperty("os.name").startsWith("Win")) {
            throw new AssertionError("fastText doesn't support Windows.");
        }

        logger.info("----------Test fastText ----------");
        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .setTypes(String.class, Classifications.class)
                        .optArtifactId("ai.djl.fasttext:cooking_stackexchange")
                        .build();

        try (ZooModel<String, Classifications> model = criteria.loadModel();
                Predictor<String, Classifications> predictor = model.newPredictor()) {
            Classifications classifications =
                    predictor.predict("Which baking dish is best to bake a banana bread ?");
            logger.info("{}", classifications);
        }
    }

    public static void testSentencePiece() throws IOException {
        if (System.getProperty("os.name").startsWith("Win")) {
            throw new AssertionError("SentencePiece doesn't support Windows.");
        }

        logger.info("----------Test SentencePiece ----------");
        Path modelFile = Paths.get("build/test/models/sententpiece_test_model.model");
        if (Files.notExists(modelFile)) {
            DownloadUtils.download(
                    "https://resources.djl.ai/test-models/sententpiece_test_model.model",
                    "build/test/models/sententpiece_test_model.model");
        }
        Path modelPath = Paths.get("build/test/models/sententpiece_test_model.model");
        try (SpTokenizer tokenizer = new SpTokenizer(modelPath)) {
            String original = "Hello World";
            List<String> tokens = tokenizer.tokenize(original);
            logger.info("{}", String.join(",", tokens));
        }
    }

    public static void testPaddle() throws IOException, ModelException, TranslateException {
        logger.info("----------Test PaddlePaddle ----------");
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .optEngine("PaddlePaddle")
                        .optArtifactId("face_detection")
                        .optFilter("flavor", "server")
                        .build();

        String url =
                "https://raw.githubusercontent.com/PaddlePaddle/PaddleHub/release/v1.5/demo/mask_detection/python/images/mask.jpg";
        try (ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);
                Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            Image img = ImageFactory.getInstance().fromUrl(url);
            DetectedObjects objs = predictor.predict(img);
            logger.info(objs.toString());
        }
    }

    private static void testTflite() throws ModelException, IOException, TranslateException {
        if (System.getProperty("os.name").startsWith("Win")) {
            throw new AssertionError("TFLite only work on macOS and Linux.");
        }
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optEngine("TFLite")
                        .optFilter("dataset", "aiyDish")
                        .build();
        try (ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
                Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Image image =
                    ImageFactory.getInstance()
                            .fromUrl("https://resources.djl.ai/images/sachertorte.jpg");
            Classifications prediction = predictor.predict(image);
            logger.info(prediction.toString());
            if (!"Sachertorte".equals(prediction.best().getClassName())) {
                throw new AssertionError("Wrong prediction result");
            }
        }
    }

    private static void testPython() throws ModelException, IOException, TranslateException {
        Criteria<Input, Output> criteria =
                Criteria.builder()
                        .setTypes(Input.class, Output.class)
                        .optModelUrls(
                                "https://mlrepo.djl.ai/model/cv/image_classification/ai/djl/python/resnet/0.0.1/pytorch.tar.gz")
                        .optEngine("Python")
                        .build();
        Path file = Paths.get("build/test/kitten.jpg");
        DownloadUtils.download(new URL("https://resources.djl.ai/images/kitten.jpg"), file, null);
        try (ZooModel<Input, Output> model = criteria.loadModel();
                Predictor<Input, Output> predictor = model.newPredictor()) {
            Input input = new Input();
            input.add("data", Files.readAllBytes(file));
            input.addProperty("Content-Type", "image/jpeg");
            Output output = predictor.predict(input);
            String classification = output.getData().getAsString();
            logger.info(classification);
        }
    }

    private static void testXgboost() throws ModelException, IOException, TranslateException {
        if (System.getProperty("os.name").startsWith("Win")) {
            throw new AssertionError("Xgboost only work on macOS and Linux.");
        }
        Path modelDir = Paths.get("build/model");
        DownloadUtils.download(
                "https://resources.djl.ai/test-models/xgboost/regression.json",
                modelDir.resolve("regression.json").toString());
        try (Model model = Model.newInstance("XGBoost")) {
            model.load(Paths.get("build/model"), "regression");
            Predictor<NDList, NDList> predictor = model.newPredictor(new NoopTranslator());
            try (NDManager manager = NDManager.newBaseManager()) {
                NDArray array = manager.ones(new Shape(10, 13));
                NDList output = predictor.predict(new NDList(array));
                float[] result = output.singletonOrThrow().toFloatArray();
                logger.info(Arrays.toString(result));
                if (result.length != 10) {
                    throw new AssertionError("Wrong prediction result");
                }
            }
        }
    }
}
