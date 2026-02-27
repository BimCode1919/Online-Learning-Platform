package org.oln.onlinelearningplatform.service.aiagent.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.oln.onlinelearningplatform.dto.AIOptionDTO;
import org.oln.onlinelearningplatform.dto.AIQuestionDTO;
import org.oln.onlinelearningplatform.dto.AIQuizResponse;
import org.oln.onlinelearningplatform.dto.OptionRequestDTO;
import org.oln.onlinelearningplatform.entity.Quiz;
import org.oln.onlinelearningplatform.service.aiagent.AIQuizService;
import org.oln.onlinelearningplatform.service.aiagent.QuizGenerator;
import org.oln.onlinelearningplatform.service.quiz.QuestionService;
import org.oln.onlinelearningplatform.service.quiz.QuizService;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIQuizServiceImpl implements AIQuizService {
    private final QuizGenerator quizGenerator; // Đã được cấu hình ở Bước 4
    private final QuestionService questionService;
    private final QuizService quizService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void createQuizFromYoutubeContent(Long lessonId, String content) {
        String rawJson = quizGenerator.generate(content);

        try {
            int startIndex = rawJson.indexOf("{");
            int endIndex = rawJson.lastIndexOf("}");

            if (startIndex == -1 || endIndex == -1) {
                throw new RuntimeException("AI không trả về đúng định dạng JSON");
            }

            String cleanedJson = rawJson.substring(startIndex, endIndex + 1);

            // Parse vào DTO
            AIQuizResponse response = objectMapper.readValue(cleanedJson, AIQuizResponse.class);

            List<AIQuestionDTO> aiQuestions = response.getQuestions();

            if (aiQuestions == null || aiQuestions.isEmpty()) {
                throw new RuntimeException("AI trả về danh sách câu hỏi rỗng");
            }

            // 2. Tạo Quiz cho Lesson (Đảm bảo hàm này trả về Quiz đã lưu)
            Quiz quiz = quizService.createQuizForLesson(lessonId, "MEDIUM");

            // 3. Duyệt mảng và lưu từng câu hỏi
            for (AIQuestionDTO q : aiQuestions) {
                questionService.addQuestion(
                        quiz.getId(),
                        q.getQuestionText(),
                        q.getExplanation(),
                        convertOptions(q.getOptions())
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý câu hỏi: " + e.getMessage());
        }
    }

    private List<OptionRequestDTO> convertOptions(List<AIOptionDTO> aiOptions) {
        List<OptionRequestDTO> options = new ArrayList<>();
        for (AIOptionDTO aiOption : aiOptions) {
            OptionRequestDTO dto = new OptionRequestDTO();
            dto.setOptionText(aiOption.getOptionText());
            dto.setCorrect(aiOption.isCorrect());
            options.add(dto);
        }
        return options;
    }
}
