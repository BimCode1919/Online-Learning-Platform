package org.oln.onlinelearningplatform.service.quiz.impl;

import org.oln.onlinelearningplatform.dto.OptionRequestDTO;
import org.oln.onlinelearningplatform.entity.Option;
import org.oln.onlinelearningplatform.entity.Question;
import org.oln.onlinelearningplatform.entity.Quiz;
import org.oln.onlinelearningplatform.repository.OptionRepository;
import org.oln.onlinelearningplatform.repository.QuestionRepository;
import org.oln.onlinelearningplatform.repository.QuizRepository;
import org.oln.onlinelearningplatform.service.quiz.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository; // Thêm OptionRepository

    public QuestionServiceImpl(QuizRepository quizRepository,
                               QuestionRepository questionRepository,
                               OptionRepository optionRepository) { // Thêm vào constructor
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }

    @Override
    @Transactional
    public Question addQuestion(Long quizId, String questionText, String explanation, List<OptionRequestDTO> options) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz với ID: " + quizId));

        // Tạo và lưu câu hỏi
        Question question = new Question();
        question.setQuestionText(questionText);
        question.setExplanation(explanation);
        question.setQuiz(quiz);

        Question savedQuestion = questionRepository.save(question);

        // Tạo và lưu các option
        for (OptionRequestDTO optionDTO : options) {
            Option option = new Option();
            option.setOptionText(optionDTO.getOptionText());
            option.setCorrect(optionDTO.isCorrect());
            option.setQuestion(savedQuestion);
            optionRepository.save(option);
        }

        return savedQuestion;
    }

    @Override
    @Transactional
    public Long deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi với ID: " + questionId));

        Long quizId = question.getQuiz().getId();
        questionRepository.delete(question);
        return quizId;
    }
}