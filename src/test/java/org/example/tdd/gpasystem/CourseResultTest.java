package org.example.tdd.gpasystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourseResultTest {

    private final Course dummyCourse = new Course("CS101", "Intro to Programming", 3);
    private final Grade passingGrade = new Grade("B", 3.0);
    private final Grade failingGrade = new Grade("F", 0.0);

    @Test
    void happyPath_shouldStoreValidDataCorrectly() {
        CourseResult result = new CourseResult(dummyCourse, 85.0, passingGrade);
        assertEquals(85.0, result.getPercentage());
        assertEquals(passingGrade, result.getGrade());
        assertEquals(dummyCourse, result.getCourse());
    }


    @Test
    void boundaryLine_shouldAllowExactMinimumOfZeroPercent() {
        CourseResult result = new CourseResult(dummyCourse, 0.0, failingGrade);
        assertEquals(0.0, result.getPercentage());
    }

    @Test
    void boundaryLine_shouldAllowExactMaximumOfOneHundredPercent() {
        CourseResult result = new CourseResult(dummyCourse, 100.0, passingGrade);
        assertEquals(100.0, result.getPercentage());
    }

   
    @Test
    void sadPath_shouldThrowExceptionForNegativePercentage() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CourseResult(dummyCourse, -0.1, failingGrade);
        });
    }

    @Test
    void sadPath_shouldThrowExceptionForPercentageOverOneHundred() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CourseResult(dummyCourse, 100.1, passingGrade);
        });
    }

    @Test
    void sadPath_shouldThrowExceptionWhenCourseIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new CourseResult(null, 80.0, passingGrade);
        });
    }
}
