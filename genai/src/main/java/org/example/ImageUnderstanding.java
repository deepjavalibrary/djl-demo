package org.example;

import ai.djl.ModelException;
import ai.djl.genai.gemini.Gemini;
import ai.djl.genai.openai.ChatInput;
import ai.djl.genai.openai.ChatOutput;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.DownloadUtils;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageUnderstanding {

    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        String url = Gemini.GEMINI_2_5_FLASH.getChatCompletionsUrl();
        Criteria<ChatInput, ChatOutput> criteria =
                Criteria.builder()
                        .setTypes(ChatInput.class, ChatOutput.class)
                        .optModelUrls(url)
                        .build();
        DownloadUtils.download("https://resources.djl.ai/images/kitten.jpg", "build/kitten.jpg");
        byte[] data = Files.readAllBytes(Paths.get("build/kitten.jpg"));

        try (ZooModel<ChatInput, ChatOutput> model = criteria.loadModel();
                Predictor<ChatInput, ChatOutput> predictor = model.newPredictor()) {
            ChatInput in =
                    ChatInput.image(data, "image/jpeg")
                            .model(Gemini.GEMINI_2_5_FLASH.name())
                            .build();
            ChatOutput ret = predictor.predict(in);
            System.out.println(ret.getTextOutput());
        }
    }
}
