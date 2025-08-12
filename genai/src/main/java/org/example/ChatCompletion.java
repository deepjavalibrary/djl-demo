package org.example;

import ai.djl.ModelException;
import ai.djl.genai.gemini.Gemini;
import ai.djl.genai.openai.ChatInput;
import ai.djl.genai.openai.ChatOutput;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class ChatCompletion {

    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        String url = Gemini.GEMINI_2_5_FLASH.getChatCompletionsUrl();
        Criteria<ChatInput, ChatOutput> criteria =
                Criteria.builder()
                        .setTypes(ChatInput.class, ChatOutput.class)
                        .optModelUrls(url)
                        .build();

        try (ZooModel<ChatInput, ChatOutput> model = criteria.loadModel();
                Predictor<ChatInput, ChatOutput> predictor = model.newPredictor()) {
            ChatInput in =
                    ChatInput.text("Say this is a test.")
                            .model(Gemini.GEMINI_2_5_FLASH.name())
                            .build();
            ChatOutput ret = predictor.predict(in);
            System.out.println(ret.getTextOutput());
        }
    }
}
