package org.example.tdd.gpasystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class SemesterRecord {
    private final int semesterNumber;
    private final List<CourseResult> courseResults = new ArrayList<>();
    private final Set<String> courseCodes = new HashSet<>();
    private boolean completed;

    public SemesterRecord(int semesterNumber) {
        if (semesterNumber < 1 || semesterNumber > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8");
        }
        this.semesterNumber = semesterNumber;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public List<CourseResult> getCourseResults() {
        return Collections.unmodifiableList(courseResults);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void addCourseResult(CourseResult result) {
        if (completed) {
            throw new IllegalStateException("Cannot add courses to a completed semester");
        }
        Objects.requireNonNull(result, "result must not be null");
        if (courseResults.size() == 5) {
            throw new IllegalStateException("A semester can contain at most 5 courses");
        }
        if (!courseCodes.add(result.getCourse().getCode())) {
            throw new IllegalArgumentException("Course already exists in this semester: "
                    + result.getCourse().getCode());
        }
        courseResults.add(result);
    }

    public void complete() {
        if (courseResults.size() < 4 || courseResults.size() > 5) {
            throw new IllegalStateException("A completed semester must contain 4 or 5 courses");
        }
        completed = true;
    }
}
