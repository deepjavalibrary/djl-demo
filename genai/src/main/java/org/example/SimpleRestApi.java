package org.example;

import ai.djl.ModelException;
import ai.djl.genai.gemini.Gemini;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class SimpleRestApi {

    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        String url = Gemini.GEMINI_2_5_FLASH.getChatCompletionsUrl();
        Criteria<String, String> criteria =
                Criteria.builder().setTypes(String.class, String.class).optModelUrls(url).build();

        try (ZooModel<String, String> model = criteria.loadModel();
                Predictor<String, String> predictor = model.newPredictor()) {
            String json =
                    """
                    {
                        "model": "gemini-2.5-flash",
                        "messages": [
                            {"role": "user", "content": "Say this is a test."}
                        ]
                    }
                    """;
            String ret = predictor.predict(json);
            System.out.println(ret);
        }
    }
}
