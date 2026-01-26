package org.oln.onlinelearningplatform.dto;

import org.oln.onlinelearningplatform.entity.Course;

/**
 * DTO chứa thông tin progress của một course
 */
public class CourseProgressDTO {
    private Course course;
    private int totalLessons;
    private int completedLessons;
    private double progressPercentage;
    private String lastAccessedDate;

    public CourseProgressDTO() {
    }

    public CourseProgressDTO(Course course, int totalLessons, int completedLessons) {
        this.course = course;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
        this.progressPercentage = totalLessons > 0
                ? (completedLessons * 100.0 / totalLessons)
                : 0.0;
    }

    // Getters and Setters
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(int completedLessons) {
        this.completedLessons = completedLessons;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getLastAccessedDate() {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(String lastAccessedDate) {
        this.lastAccessedDate = lastAccessedDate;
    }
}