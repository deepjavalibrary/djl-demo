package com.examples;

import ai.djl.modality.Classifications;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Pipeline;
import ai.djl.translate.ServingTranslator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CustomTranslator implements ServingTranslator {

    private List<String> classes;

    @Override
    public NDList processInput(TranslatorContext ctx, Input input) throws Exception {
        byte[] data = input.getAsBytes(0);
        ImageFactory factory = ImageFactory.getInstance();
        Image image = factory.fromInputStream(new ByteArrayInputStream(data));

        NDArray array = image.toNDArray(ctx.getNDManager());
        Pipeline pipeline = new Pipeline();
        pipeline.add(new CenterCrop());
        pipeline.add(new Resize(224, 224));
        pipeline.add(new ToTensor());
        return pipeline.transform(new NDList(array));
    }

    @Override
    public Output processOutput(TranslatorContext ctx, NDList list) {
        NDArray probabilitiesNd = list.singletonOrThrow();
        probabilitiesNd = probabilitiesNd.softmax(0);
        Classifications classifications = new Classifications(classes, probabilitiesNd);

        Output output = new Output(200, "OK");
        output.add(classifications.toJson());
        return output;
    }

    @Override
    public void setArguments(Map<String, ?> arguments) {}

    @Override
    public void prepare(TranslatorContext ctx) throws IOException {
        if (classes == null) {
            classes = ctx.getModel().getArtifact("synset.txt", Utils::readLines);
        }
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}
