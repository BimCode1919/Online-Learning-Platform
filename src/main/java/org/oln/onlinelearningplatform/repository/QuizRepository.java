package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Cách 1: Dùng EntityGraph
    @EntityGraph(attributePaths = {"questions", "questions.options"})
    @Query("SELECT q FROM Quiz q WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    // Cách 2: Dùng JOIN FETCH
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions qt LEFT JOIN FETCH qt.options WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestionsAndOptions(@Param("id") Long id);

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "LEFT JOIN FETCH q.lesson l " +
            "LEFT JOIN FETCH l.course c " +
            "WHERE q.id = :id")
    Optional<Quiz> findByIdWithAllData(@Param("id") Long id);
}
