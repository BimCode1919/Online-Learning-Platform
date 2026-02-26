package org.oln.onlinelearningplatform.service.aiagent.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.oln.onlinelearningplatform.dto.AIOptionDTO;
import org.oln.onlinelearningplatform.dto.AIQuestionDTO;
import org.oln.onlinelearningplatform.dto.OptionRequestDTO;
import org.oln.onlinelearningplatform.entity.Quiz;
import org.oln.onlinelearningplatform.service.aiagent.AIQuizService;
import org.oln.onlinelearningplatform.service.aiagent.QuizGenerator;
import org.oln.onlinelearningplatform.service.quiz.QuestionService;
import org.oln.onlinelearningplatform.service.quiz.QuizService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIQuizServiceImpl implements AIQuizService {
    private final QuizGenerator quizGenerator; // Đã được cấu hình ở Bước 4
    private final QuestionService questionService;
    private final QuizService quizService;

    @Override
    @Transactional
    public void createQuizFromYoutubeContent(Long lessonId, String content) {
        // 1. AI tự động làm hết: Gọi Gemini -> Nhận JSON -> Parse sang List<AIQuestionDTO>
        List<AIQuestionDTO> aiQuestions = quizGenerator.generate(content);

        // 2. Tạo Quiz mới (Dùng logic cũ của bạn)
        Quiz quiz = quizService.createQuizForLesson(lessonId, "MEDIUM");

        // 3. Lưu vào DB (Dùng hàm addQuestion cũ của bạn)
        for (AIQuestionDTO q : aiQuestions) {
            questionService.addQuestion(
                    quiz.getId(),
                    q.getQuestionText(),
                    q.getExplanation(),
                    convertOptions(q.getOptions()) // Hàm phụ để map sang OptionRequestDTO
            );
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
