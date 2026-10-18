package org.example.tdd.gpasystem;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class Student implements IStudent {
    private final String id;
    private final String name;
    private final Map<Integer, SemesterRecord> semesters = new TreeMap<>();

    public Student(String id, String name) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Student id must not be blank");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name must not be blank");
        }
        this.id = id.trim();
        this.name = name.trim();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void addSemester(SemesterRecord semester) {
        Objects.requireNonNull(semester, "semester must not be null");
        if (semesters.putIfAbsent(semester.getSemesterNumber(), semester) != null) {
            throw new IllegalArgumentException("Semester already exists: " + semester.getSemesterNumber());
        }
    }

    @Override
    public SemesterRecord getSemester(int semesterNumber) {
        return semesters.get(semesterNumber);
    }

    @Override
    public Map<Integer, SemesterRecord> getSemesters() {
        return Collections.unmodifiableMap(semesters);
    }
}
