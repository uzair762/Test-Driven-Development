package org.example.tdd.gpasystem;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GpaCalculatorTest {
    private final GpaCalculator calculator = new GpaCalculator();
    private final GradingPolicyManager policy = GradingPolicyManager.standardFourPointScale();
    private final TestDataGenerator data = new TestDataGenerator();

    @Test
    void calculatesCreditWeightedSemesterGpa() {
        Student student = new Student("S1", "Student One");
        SemesterRecord semester = new SemesterRecord(1);
        semester.addCourseResult(policy.createCourseResult(new Course("A", "High credit", 3), 95));
        semester.addCourseResult(policy.createCourseResult(new Course("B", "Low credit", 1), 75));
        student.addSemester(semester);

        assertEquals(3.5, calculator.calculateSemesterGpa(student, 1), 0.0001);
    }

    @Test
    void calculatesGpaForFakeStudentWithoutValidatingItsIdentity() {
        FakeStudent fake = new FakeStudent(null, "Ada Lovelace");
        fake.addSemester(fakeSemester("TARGET", 85));

        assertEquals(2.25, calculator.calculateSemesterGpa(fake, 1), 0.0001);
    }

    @Test
    void calculatesCoursePerformanceFromFakeStudentsWithInvalidIds() {
        FakeStudent high = new FakeStudent("", "Ada Lovelace");
        high.addSemester(fakeSemester("TARGET", 95));
        FakeStudent low = new FakeStudent(null, "Grace Hopper");
        low.addSemester(fakeSemester("TARGET", 65));

        StudentPerformanceSummary result = calculator.coursePerformance(Arrays.asList(high, low), "target");

        assertEquals("Ada Lovelace", result.getBest().getStudent().getName());
        assertEquals("Grace Hopper", result.getWorst().getStudent().getName());
        assertEquals(2, result.getPerformances().size());
    }

    @Test
    void calculatesCumulativeGpaOnlyFromCompletedSemesters() {
        Student student = data.createStudentWithSemesters("S1", "Student One", 1, 85);
        SemesterRecord inProgress = new SemesterRecord(2);
        inProgress.addCourseResult(policy.createCourseResult(new Course("CURRENT", "Current", 3), 0));
        student.addSemester(inProgress);

        assertEquals(3.0, calculator.calculateCumulativeGpa(student), 0.0001);
    }

    @Test
    void calculatesCumulativeGpaAcrossCompletedCourseCredits() {
        Student student = data.createStudentWithSemesters("S1", "Student One", 2, 95);
        assertEquals(4.0, calculator.calculateCumulativeGpa(student), 0.0001);
    }

    @Test
    void cumulativeGpaWeightsEveryCompletedCourseRatherThanSemesterAverages() {
        Student student = new Student("S1", "Student One");
        SemesterRecord firstSemester = new SemesterRecord(1);
        firstSemester.addCourseResult(policy.createCourseResult(new Course("A", "Three credits", 3), 95));
        firstSemester.addCourseResult(policy.createCourseResult(new Course("B", "One credit", 1), 85));
        firstSemester.addCourseResult(policy.createCourseResult(new Course("C", "One credit", 1), 75));
        firstSemester.addCourseResult(policy.createCourseResult(new Course("D", "One credit", 1), 65));
        firstSemester.complete();
        student.addSemester(firstSemester);
        SemesterRecord secondSemester = new SemesterRecord(2);
        for (int courseNumber = 1; courseNumber <= 4; courseNumber++) {
            secondSemester.addCourseResult(policy.createCourseResult(
                    new Course("E" + courseNumber, "Failed course " + courseNumber, 1), 50));
        }
        secondSemester.complete();
        student.addSemester(secondSemester);

        assertEquals(1.8, calculator.calculateCumulativeGpa(student), 0.0001);
    }

    @Test
    void calculatesSemesterGpaUsingPlusMinusScale() {
        GradingPolicyManager plusMinus = GradingPolicyManager.plusMinusScale();
        Student student = new Student("S1", "Student One");
        SemesterRecord semester = new SemesterRecord(1);
        semester.addCourseResult(plusMinus.createCourseResult(new Course("A1", "A minus course", 3), 90));
        semester.addCourseResult(plusMinus.createCourseResult(new Course("B1", "B plus course", 3), 87));
        semester.addCourseResult(plusMinus.createCourseResult(new Course("C1", "C minus course", 3), 70));
        semester.addCourseResult(plusMinus.createCourseResult(new Course("D1", "Just below C minus", 3), 69.99));
        semester.complete();
        student.addSemester(semester);

        // (3.7 + 3.3 + 1.7 + 1.3) / 4 = 2.5
        assertEquals(2.5, calculator.calculateSemesterGpa(student, 1), 0.0001);
    }

    @Test
    void rejectsNullStudentAndMissingOrEmptySemester() {
        assertThrows(NullPointerException.class, () -> calculator.calculateSemesterGpa(null, 1));
        Student student = new Student("S1", "Student One");
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateSemesterGpa(student, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateSemesterGpa(student, 9));
        student.addSemester(new SemesterRecord(1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateSemesterGpa(student, 1));
    }

    @Test
    void rejectsCumulativeGpaWhenNoSemesterHasBeenCompleted() {
        Student student = new Student("S1", "Student One");
        student.addSemester(new SemesterRecord(1));
        assertThrows(NullPointerException.class, () -> calculator.calculateCumulativeGpa(null));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateCumulativeGpa(student));
    }

    @Test
    void summarizesSemesterPerformancesAndReturnsNearestAverageStudent() {
        List<Student> cohort = data.createCompletedSemesterCohort();

        StudentPerformanceSummary result = calculator.semesterPerformance(cohort, 1);

        assertEquals(3, result.getPerformances().size());
        assertEquals("S001", result.getBest().getStudent().getId());
        assertEquals("S003", result.getWorst().getStudent().getId());
        assertEquals("S002", result.getAveragePerformer().getStudent().getId());
        assertEquals(8.0 / 3.0, result.getAverageGpa(), 0.0001);
    }

    @Test
    void excludesStudentsWithoutCompletedSemesterAndBreaksTiesById() {
        Student zed = data.createStudent("Z", "Zed", 85);
        Student amy = data.createStudent("A", "Amy", 85);
        Student inProgress = new Student("I", "In Progress");
        inProgress.addSemester(new SemesterRecord(1));

        StudentPerformanceSummary result = calculator.semesterPerformance(
                Arrays.asList(zed, inProgress, amy), 1);

        assertEquals("A", result.getBest().getStudent().getId());
        assertEquals("A", result.getWorst().getStudent().getId());
        assertEquals("A", result.getAveragePerformer().getStudent().getId());
        assertEquals(2, result.getPerformances().size());
    }

    @Test
    void performanceSummaryListsStudentsFromHighestToLowestGpa() {
        StudentPerformanceSummary result = calculator.semesterPerformance(
                data.createCompletedSemesterCohort(), 1);

        assertEquals("S001", result.getPerformances().get(0).getStudent().getId());
        assertEquals("S002", result.getPerformances().get(1).getStudent().getId());
        assertEquals("S003", result.getPerformances().get(2).getStudent().getId());
    }

    @Test
    void summarizesCoursePerformancesAcrossCompletedSemesters() {
        Student high = data.createStudentWithSemesters("S1", "High", 2, 95);
        Student low = data.createStudentWithSemesters("S2", "Low", 1, 65);
        StudentPerformanceSummary result = calculator.coursePerformance(Arrays.asList(high, low), "c1");

        assertEquals(2, result.getPerformances().size());
        assertEquals("S1", result.getBest().getStudent().getId());
        assertEquals("S2", result.getWorst().getStudent().getId());
        assertEquals(2.5, result.getAverageGpa(), 0.0001);
    }

    @Test
    void excludesCourseAttemptsFromInProgressSemesters() {
        Student student = new Student("S1", "Student One");
        SemesterRecord completed = new SemesterRecord(1);
        completed.addCourseResult(policy.createCourseResult(new Course("TARGET", "Target", 3), 95));
        for (int courseNumber = 1; courseNumber <= 3; courseNumber++) {
            completed.addCourseResult(policy.createCourseResult(
                    new Course("C" + courseNumber, "Course " + courseNumber, 3), 95));
        }
        completed.complete();
        student.addSemester(completed);
        SemesterRecord inProgress = new SemesterRecord(2);
        inProgress.addCourseResult(policy.createCourseResult(new Course("TARGET", "Target", 3), 0));
        student.addSemester(inProgress);

        StudentPerformanceSummary result = calculator.coursePerformance(
                Collections.singletonList(student), "TARGET");

        assertEquals("S1", result.getBest().getStudent().getId());
        assertEquals(4.0, result.getAverageGpa(), 0.0001);
    }

    @Test
    void rejectsInvalidSummaryInputsAndEmptyScopes() {
        assertThrows(IllegalArgumentException.class, () -> calculator.semesterPerformance(
                data.createCompletedSemesterCohort(), 0));
        assertThrows(IllegalArgumentException.class, () -> calculator.semesterPerformance(
                data.createCompletedSemesterCohort(), 9));
        assertThrows(NullPointerException.class, () -> calculator.semesterPerformance(null, 1));
        assertThrows(NullPointerException.class, () -> calculator.semesterPerformance(
                Arrays.asList((Student) null), 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.semesterPerformance(
                Collections.singletonList(new Student("S", "No Results")), 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.coursePerformance(
                data.createCompletedSemesterCohort(), " "));
        assertThrows(NullPointerException.class, () -> calculator.coursePerformance(null, "C1"));
        assertThrows(NullPointerException.class, () -> calculator.coursePerformance(
                Arrays.asList((Student) null), "C1"));
        assertThrows(IllegalArgumentException.class, () -> calculator.coursePerformance(
                data.createCompletedSemesterCohort(), "UNKNOWN"));
    }

    private SemesterRecord fakeSemester(String targetCode, double targetPercentage) {
        SemesterRecord semester = new SemesterRecord(1);
        semester.addCourseResult(policy.createCourseResult(
                new Course(targetCode, "Target course", 3.0), targetPercentage));
        for (int courseNumber = 1; courseNumber <= 3; courseNumber++) {
            semester.addCourseResult(policy.createCourseResult(
                    new Course("OTHER" + courseNumber, "Other course " + courseNumber, 3.0), 75.0));
        }
        semester.complete();
        return semester;
    }
}
