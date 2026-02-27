package org.oln.onlinelearningplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.oln.onlinelearningplatform.service.aiagent.QuizGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Value("${langchain4j.google-ai-gemini.api-key}")
    private String apiKey;

    @Value("${langchain4j.google-ai-gemini.model-name}")
    private String modelName;

    @Bean
    public ChatLanguageModel geminiChatModel() {
        // Trả về thẳng ChatModel để tương thích với AiServices
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-flash-latest")
                .timeout(java.time.Duration.ofSeconds(60))
                .logRequestsAndResponses(true)
                .build();
    }

    @Bean
    public QuizGenerator quizGenerator(ChatLanguageModel chatModel) {
        return AiServices.builder(QuizGenerator.class)
                .chatLanguageModel(chatModel) // Khớp 100% với ChatModel phía trên
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}