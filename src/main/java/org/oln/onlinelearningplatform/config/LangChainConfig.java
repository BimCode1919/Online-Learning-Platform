package org.oln.onlinelearningplatform.config;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.oln.onlinelearningplatform.service.aiagent.QuizGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public GoogleAiGeminiChatModel geminiChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-flash")
                .logRequestsAndResponses(true)
                .build();
    }

    @Bean
    public QuizGenerator quizGenerator(GoogleAiGeminiChatModel model) {
        // SỬA TẠI ĐÂY: Dùng chatModel() thay vì chatLanguageModel()
        return AiServices.builder(QuizGenerator.class)
                .chatModel(model)
                .build();
    }
}
