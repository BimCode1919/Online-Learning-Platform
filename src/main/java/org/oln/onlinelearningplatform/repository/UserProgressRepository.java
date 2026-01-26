package org.oln.onlinelearningplatform.repository;

import org.oln.onlinelearningplatform.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {


    // Tìm progress record của một user cho một lesson cụ thể

    @Query("SELECT up FROM UserProgress up WHERE up.user.id = :userId AND up.lesson.id = :lessonId")
    Optional<UserProgress> findByUserIdAndLessonId(@Param("userId") Long userId,
                                                   @Param("lessonId") Long lessonId);

    void deleteByUserId(Long id);
}
