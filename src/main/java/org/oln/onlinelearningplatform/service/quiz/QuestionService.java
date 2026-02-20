package org.oln.onlinelearningplatform.service.quiz;

import org.oln.onlinelearningplatform.dto.OptionRequestDTO;
import org.oln.onlinelearningplatform.entity.Question;

import java.util.List;

public interface QuestionService {
    Question addQuestion(Long quizId, String questionText, String explanation, List<OptionRequestDTO> options);
    Long deleteQuestion(Long questionId);
}
