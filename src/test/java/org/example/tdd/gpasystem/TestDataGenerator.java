package org.example.tdd.gpasystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TestDataGenerator {
    private final GradingPolicyManager gradingPolicy = GradingPolicyManager.standardFourPointScale();

    public List<Student> createCompletedSemesterCohort() {
        return Arrays.asList(
                createStudent("S001", "Amina", 95.0),
                createStudent("S002", "Bilal", 82.0),
                createStudent("S003", "Chen", 68.0));
    }

    public Student createStudent(String id, String name, double percentage) {
        Student student = new Student(id, name);
        SemesterRecord semester = new SemesterRecord(1);
        for (int courseNumber = 1; courseNumber <= 4; courseNumber++) {
            Course course = new Course("C" + courseNumber, "Course " + courseNumber, 3.0);
            semester.addCourseResult(gradingPolicy.createCourseResult(course, percentage));
        }
        semester.complete();
        student.addSemester(semester);
        return student;
    }

    public Student createStudentWithSemesters(String id, String name, int completedSemesters,
                                              double percentage) {
        if (completedSemesters < 0 || completedSemesters > 8) {
            throw new IllegalArgumentException("Completed semesters must be between 0 and 8");
        }
        Student student = new Student(id, name);
        for (int semesterNumber = 1; semesterNumber <= completedSemesters; semesterNumber++) {
            SemesterRecord semester = new SemesterRecord(semesterNumber);
            for (int courseNumber = 1; courseNumber <= 4; courseNumber++) {
                Course course = new Course("C" + courseNumber,
                        "Semester " + semesterNumber + " course " + courseNumber, 3.0);
                semester.addCourseResult(gradingPolicy.createCourseResult(course, percentage));
            }
            semester.complete();
            student.addSemester(semester);
        }
        return student;
    }

    public List<Student> mutableCopyOfCohort() {
        return new ArrayList<>(createCompletedSemesterCohort());
    }
}
