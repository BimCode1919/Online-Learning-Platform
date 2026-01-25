package org.oln.onlinelearningplatform.entity;

import java.util.List;

/**
 * DTO chứa tất cả thống kê cho dashboard
 */
public class DashboardStatsDTO {
    private int totalCoursesEnrolled;
    private int totalLessonsCompleted;
    private int totalQuizzesTaken;
    private double averageQuizScore;
    private List<CourseProgressDTO> coursesProgress;

    public DashboardStatsDTO() {
    }

    // Getters and Setters
    public int getTotalCoursesEnrolled() {
        return totalCoursesEnrolled;
    }

    public void setTotalCoursesEnrolled(int totalCoursesEnrolled) {
        this.totalCoursesEnrolled = totalCoursesEnrolled;
    }

    public int getTotalLessonsCompleted() {
        return totalLessonsCompleted;
    }

    public void setTotalLessonsCompleted(int totalLessonsCompleted) {
        this.totalLessonsCompleted = totalLessonsCompleted;
    }

    public int getTotalQuizzesTaken() {
        return totalQuizzesTaken;
    }

    public void setTotalQuizzesTaken(int totalQuizzesTaken) {
        this.totalQuizzesTaken = totalQuizzesTaken;
    }

    public double getAverageQuizScore() {
        return averageQuizScore;
    }

    public void setAverageQuizScore(double averageQuizScore) {
        this.averageQuizScore = averageQuizScore;
    }

    public List<CourseProgressDTO> getCoursesProgress() {
        return coursesProgress;
    }

    public void setCoursesProgress(List<CourseProgressDTO> coursesProgress) {
        this.coursesProgress = coursesProgress;
    }
}