package org.example.tdd.gpasystem;

import java.util.Objects;

public final class StudentGpa {
    private final IStudent student;
    private final double gpa;

    public StudentGpa(IStudent student, double gpa) {
        this.student = Objects.requireNonNull(student, "student must not be null");
        if (!Double.isFinite(gpa) || gpa < 0.0 || gpa > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0 and 4");
        }
        this.gpa = gpa;
    }

    public IStudent getStudent() {
        return student;
    }

    public double getGpa() {
        return gpa;
    }
}
