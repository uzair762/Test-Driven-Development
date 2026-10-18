package org.example.tdd.gpasystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class GpaCalculator {
    public double calculateSemesterGpa(IStudent student, int semesterNumber) {
        Objects.requireNonNull(student, "student must not be null");
        validateSemesterNumber(semesterNumber);
        SemesterRecord semester = student.getSemester(semesterNumber);
        if (semester == null) {
            throw new IllegalArgumentException("Student has no semester " + semesterNumber);
        }
        return weightedGpa(semester.getCourseResults());
    }

    public double calculateCumulativeGpa(IStudent student) {
        Objects.requireNonNull(student, "student must not be null");
        List<CourseResult> completedResults = new ArrayList<>();
        for (SemesterRecord semester : student.getSemesters().values()) {
            if (semester.isCompleted()) {
                completedResults.addAll(semester.getCourseResults());
            }
        }
        return weightedGpa(completedResults);
    }

    public StudentPerformanceSummary semesterPerformance(Collection<? extends IStudent> students,
                                                            int semesterNumber) {
        validateSemesterNumber(semesterNumber);
        List<StudentGpa> performances = new ArrayList<>();
        for (IStudent student : validatedStudents(students)) {
            SemesterRecord semester = student.getSemester(semesterNumber);
            if (semester != null && semester.isCompleted()) {
                performances.add(new StudentGpa(student, weightedGpa(semester.getCourseResults())));
            }
        }
        return summarize(performances);
    }

    public StudentPerformanceSummary coursePerformance(Collection<? extends IStudent> students, String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code must not be blank");
        }
        String normalizedCode = courseCode.trim();
        List<StudentGpa> performances = new ArrayList<>();
        for (IStudent student : validatedStudents(students)) {
            List<CourseResult> matchingResults = new ArrayList<>();
            for (SemesterRecord semester : student.getSemesters().values()) {
                if (semester.isCompleted()) {
                    for (CourseResult result : semester.getCourseResults()) {
                        if (result.getCourse().getCode().equalsIgnoreCase(normalizedCode)) {
                            matchingResults.add(result);
                        }
                    }
                }
            }
            if (!matchingResults.isEmpty()) {
                performances.add(new StudentGpa(student, weightedGpa(matchingResults)));
            }
        }
        return summarize(performances);
    }

    private List<IStudent> validatedStudents(Collection<? extends IStudent> students) {
        Objects.requireNonNull(students, "students must not be null");
        List<IStudent> copy = new ArrayList<>(students);
        for (IStudent student : copy) {
            Objects.requireNonNull(student, "students must not contain null");
        }
        return copy;
    }

    private void validateSemesterNumber(int semesterNumber) {
        if (semesterNumber < 1 || semesterNumber > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8");
        }
    }

    private double weightedGpa(List<CourseResult> results) {
        if (results.isEmpty()) {
            throw new IllegalArgumentException("At least one course result is required");
        }
        double weightedPoints = 0.0;
        double totalCredits = 0.0;
        for (CourseResult result : results) {
            weightedPoints += result.getGrade().getPoints() * result.getCourse().getCredits();
            totalCredits += result.getCourse().getCredits();
        }
        return weightedPoints / totalCredits;
    }

    private StudentPerformanceSummary summarize(List<StudentGpa> performances) {
        if (performances.isEmpty()) {
            throw new IllegalArgumentException("No students have results for the requested scope");
        }
        Comparator<StudentGpa> byGpaThenId = Comparator.comparingDouble(StudentGpa::getGpa)
                .thenComparing(result -> result.getStudent().getId(), Comparator.nullsFirst(String::compareTo));
        Comparator<StudentGpa> bestFirst = Comparator.comparingDouble(StudentGpa::getGpa).reversed()
                .thenComparing(result -> result.getStudent().getId(), Comparator.nullsFirst(String::compareTo));
        StudentGpa best = performances.stream().min(bestFirst).get();
        StudentGpa worst = performances.stream().min(byGpaThenId).get();
        double averageGpa = performances.stream().mapToDouble(StudentGpa::getGpa).average().getAsDouble();
        ToDoubleFunction<StudentGpa> distanceFromAverage =
                result -> Math.abs(result.getGpa() - averageGpa);
        StudentGpa averagePerformer = performances.stream()
                .min(Comparator.comparingDouble(distanceFromAverage)
                        .thenComparing(result -> result.getStudent().getId(),
                                Comparator.nullsFirst(String::compareTo)))
                .get();
        performances.sort(bestFirst);
        return new StudentPerformanceSummary(performances, averageGpa, best, worst, averagePerformer);
    }
}
