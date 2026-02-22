package org.oln.onlinelearningplatform.service.quiz;

import org.oln.onlinelearningplatform.entity.Quiz;

public interface QuizService {
    Quiz createQuizForLesson(Long lessonId, String difficulty);

    void deleteQuiz(Long quizId);

}
