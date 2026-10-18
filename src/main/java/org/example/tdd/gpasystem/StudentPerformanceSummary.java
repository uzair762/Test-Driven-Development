package org.example.tdd.gpasystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StudentPerformanceSummary {
    private final List<StudentGpa> performances;
    private final double averageGpa;
    private final StudentGpa best;
    private final StudentGpa worst;
    private final StudentGpa averagePerformer;

    StudentPerformanceSummary(List<StudentGpa> performances, double averageGpa,
                              StudentGpa best, StudentGpa worst, StudentGpa averagePerformer) {
        this.performances = Collections.unmodifiableList(new ArrayList<>(performances));
        this.averageGpa = averageGpa;
        this.best = best;
        this.worst = worst;
        this.averagePerformer = averagePerformer;
    }

    public List<StudentGpa> getPerformances() {
        return performances;
    }

    public double getAverageGpa() {
        return averageGpa;
    }

    public StudentGpa getBest() {
        return best;
    }

    public StudentGpa getWorst() {
        return worst;
    }

    public StudentGpa getAveragePerformer() {
        return averagePerformer;
    }
}
