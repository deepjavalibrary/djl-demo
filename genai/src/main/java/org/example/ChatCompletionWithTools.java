package org.example;

import ai.djl.ModelException;
import ai.djl.genai.FunctionUtils;
import ai.djl.genai.gemini.Gemini;
import ai.djl.genai.openai.ChatInput;
import ai.djl.genai.openai.ChatOutput;
import ai.djl.genai.openai.Function;
import ai.djl.genai.openai.Tool;
import ai.djl.genai.openai.ToolCall;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.util.JsonUtils;

import java.io.IOException;
import java.lang.reflect.Method;

public class ChatCompletionWithTools {

    public static void main(String[] args)
            throws ModelException, IOException, TranslateException, ReflectiveOperationException {
        String url = Gemini.GEMINI_2_5_FLASH.getChatCompletionsUrl();
        Criteria<ChatInput, ChatOutput> criteria =
                Criteria.builder()
                        .setTypes(ChatInput.class, ChatOutput.class)
                        .optModelUrls(url)
                        .build();

        try (ZooModel<ChatInput, ChatOutput> model = criteria.loadModel();
                Predictor<ChatInput, ChatOutput> predictor = model.newPredictor()) {

            Method method = ChatCompletionWithTools.class.getMethod("getWeather", String.class);
            Function function =
                    Function.function(method)
                            .description("Get the current weather in a given location")
                            .build();
            ChatInput in =
                    ChatInput.text("What's the weather like in New York today?")
                            .model(Gemini.GEMINI_2_5_FLASH.name())
                            .tools(Tool.of(function))
                            .toolChoice("auto")
                            .build();
            ChatOutput ret = predictor.predict(in);
            ToolCall toolCall = ret.getToolCall();
            System.out.println("Tool call: " + JsonUtils.toJson(toolCall));
            String arguments = toolCall.getFunction().getArguments();
            String weather = (String) FunctionUtils.invoke(method, null, arguments);
            System.out.printf(weather);
        }
    }

    public static String getWeather(String location) {
        return "The weather in " + location + " is nice.";
    }
}
