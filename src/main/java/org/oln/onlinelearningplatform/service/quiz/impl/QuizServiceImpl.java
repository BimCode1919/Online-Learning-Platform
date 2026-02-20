package org.oln.onlinelearningplatform.service.quiz.impl;

import org.oln.onlinelearningplatform.entity.Lesson;
import org.oln.onlinelearningplatform.entity.Quiz;
import org.oln.onlinelearningplatform.repository.LessonRepository;
import org.oln.onlinelearningplatform.repository.QuizRepository;
import org.oln.onlinelearningplatform.service.quiz.QuizService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuizServiceImpl implements QuizService {

    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;

    public QuizServiceImpl(LessonRepository lessonRepository, QuizRepository quizRepository) {
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
    }

    @Override
    @Transactional
    public Quiz createQuizForLesson(Long lessonId, String difficulty) {
        // Lấy lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lesson với ID: " + lessonId));

        // Kiểm tra xem lesson đã có quiz chưa
        if (lesson.getQuiz() != null) {
            throw new RuntimeException("Bài học này đã có quiz rồi!");
        }

        // Tạo quiz mới - CHỈNH SỬA Ở ĐÂY
        Quiz quiz = new Quiz();
        quiz.setTitle("Quiz cho bài: " + lesson.getTitle());
        quiz.setType(Quiz.QuizType.MANUAL);        // Đổi thành MANUAL
        quiz.setStatus(Quiz.QuizStatus.READY);      // Đổi thành READY (vì instructor tự nhập)
        quiz.setDifficulty(difficulty);
        quiz.setCourse(lesson.getCourse());
        quiz.setLesson(lesson);

        // Lưu quiz
        Quiz savedQuiz = quizRepository.save(quiz);

        // Set quiz cho lesson
        lesson.setQuiz(savedQuiz);
        lessonRepository.save(lesson);

        return savedQuiz;
    }

    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz"));
        quizRepository.delete(quiz);
    }


}
