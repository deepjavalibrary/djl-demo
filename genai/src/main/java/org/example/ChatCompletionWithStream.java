package org.example;

import ai.djl.ModelException;
import ai.djl.genai.gemini.Gemini;
import ai.djl.genai.openai.ChatInput;
import ai.djl.genai.openai.ChatOutput;
import ai.djl.genai.openai.StreamChatOutput;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class ChatCompletionWithStream {

    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        String url = Gemini.GEMINI_2_5_FLASH.getChatCompletionsUrl();
        Criteria<ChatInput, StreamChatOutput> criteria =
                Criteria.builder()
                        .setTypes(ChatInput.class, StreamChatOutput.class)
                        .optModelUrls(url)
                        .build();

        try (ZooModel<ChatInput, StreamChatOutput> model = criteria.loadModel();
                Predictor<ChatInput, StreamChatOutput> predictor = model.newPredictor()) {
            ChatInput in =
                    ChatInput.text("Tell me a story about cat in 300 words.")
                            .model(Gemini.GEMINI_2_5_FLASH.name())
                            .stream(true)
                            .build();
            StreamChatOutput ret = predictor.predict(in);
            for (ChatOutput out : ret) {
                System.out.println(out.getTextOutput());
            }
        }
    }
}
