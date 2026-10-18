package org.example.tdd.gpasystem;

import java.util.Map;

public interface IStudent extends IPerson {
    SemesterRecord getSemester(int semesterNumber);

    Map<Integer, SemesterRecord> getSemesters();
}
