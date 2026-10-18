package org.example.tdd.gpasystem;

import java.util.Objects;

public final class CourseResult {
    private final Course course;
    private final double percentage;
    private final Grade grade;

    CourseResult(Course course, double percentage, Grade grade) {
        if (!Double.isFinite(percentage) || percentage < 0.0 || percentage > 100.0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.course = Objects.requireNonNull(course, "course must not be null");
        this.percentage = percentage;
        this.grade = Objects.requireNonNull(grade, "grade must not be null");
    }

    public Course getCourse() {
        return course;
    }

    public double getPercentage() {
        return percentage;
    }

    public Grade getGrade() {
        return grade;
    }
}
