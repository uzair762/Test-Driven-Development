package org.example.tdd.gpasystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Question 1: plus/minus grading scale (GradeBand + GradingPolicyManager).
 */
class GradingPolicyManagerTest {

    private final GradingPolicyManager policy = GradingPolicyFactory.plusMinusScale();

    private static Grade g(String letter, double points) {
        return new Grade(letter, points);
    }

    @Nested
    @DisplayName("Scores exactly at each cutoff")
    class ExactlyAtCutoff {

        @ParameterizedTest(name = "{0}% -> {1} ({2})")
        @CsvSource({
                "100.0, A,  4.0",
                "93.0,  A,  4.0",
                "90.0,  A-, 3.7",
                "87.0,  B+, 3.3",
                "83.0,  B,  3.0",
                "80.0,  B-, 2.7",
                "77.0,  C+, 2.3",
                "73.0,  C,  2.0",
                "70.0,  C-, 1.7",
                "67.0,  D+, 1.3",
                "60.0,  D,  1.0",
                "0.0,   F,  0.0"
        })
        void cutoffEarnsItsOwnBandsGrade(double percentage, String letter, double points) {
            Grade grade = policy.getGradeForPercentage(percentage);

            assertEquals(letter, grade.getLetter());
            assertEquals(points, grade.getPoints(), 0.0001);
        }

        @Test
        void everyBandsCutoffReturnsThatBandsOwnGrade() {
            for (GradeBand band : policy.getGradeBands()) {
                assertEquals(band.getGrade(), policy.getGradeForPercentage(band.getMinimumPercentage()),
                        "cutoff " + band.getMinimumPercentage());
            }
        }
    }

    @Nested
    @DisplayName("Scores just below each cutoff")
    class JustBelowCutoff {

        @ParameterizedTest(name = "nextDown({0}) -> {1} ({2})")
        @CsvSource({
                "93.0, A-, 3.7",
                "90.0, B+, 3.3",
                "87.0, B,  3.0",
                "83.0, B-, 2.7",
                "80.0, C+, 2.3",
                "77.0, C,  2.0",
                "73.0, C-, 1.7",
                "70.0, D+, 1.3",
                "67.0, D,  1.0",
                "60.0, F,  0.0"
        })
        void smallestValueBelowCutoffFallsIntoTheNextLowerBand(double cutoff, String letter, double points) {
            Grade grade = policy.getGradeForPercentage(Math.nextDown(cutoff));

            assertEquals(letter, grade.getLetter());
            assertEquals(points, grade.getPoints(), 0.0001);
        }

        @ParameterizedTest(name = "{0}% -> {1}")
        @CsvSource({
                "92.99, A-", "89.99, B+", "86.99, B", "82.99, B-", "79.99, C+",
                "76.99, C",  "72.99, C-", "69.99, D+", "66.99, D", "59.99, F"
        })
        void twoDecimalScoresJustBelowCutoff(double percentage, String letter) {
            assertEquals(letter, policy.getGradeForPercentage(percentage).getLetter());
        }

        @Test
        void everyBandsNextDownValueReturnsTheBandBelow() {
            List<GradeBand> bands = policy.getGradeBands();
            for (int i = 0; i < bands.size() - 1; i++) {
                double justBelow = Math.nextDown(bands.get(i).getMinimumPercentage());
                assertEquals(bands.get(i + 1).getGrade(), policy.getGradeForPercentage(justBelow),
                        "just below " + bands.get(i).getMinimumPercentage());
            }
        }
    }

    @Nested
    @DisplayName("Invalid percentages")
    class InvalidPercentages {

        @ParameterizedTest(name = "{0}% is rejected")
        @ValueSource(doubles = {-0.01, -1.0, -100.0, 100.01, 150.0, Double.MAX_VALUE,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
        void percentageOutsideZeroToHundredIsRejected(double percentage) {
            assertThrows(IllegalArgumentException.class, () -> policy.getGradeForPercentage(percentage));
        }

        @Test
        void justOutsideTheValidRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> policy.getGradeForPercentage(Math.nextDown(0.0)));
            assertThrows(IllegalArgumentException.class,
                    () -> policy.getGradeForPercentage(Math.nextUp(100.0)));
        }

        @Test
        void createCourseResultRejectsInvalidPercentage() {
            Course course = new Course("CS101", "Programming", 3);

            assertThrows(IllegalArgumentException.class, () -> policy.createCourseResult(course, 100.5));
            assertThrows(IllegalArgumentException.class, () -> policy.createCourseResult(course, -5));
        }
    }

    @Nested
    @DisplayName("Incomplete or invalid policy")
    class InvalidPolicy {

        @Test
        void nullPolicyTableIsRejected() {
            assertThrows(NullPointerException.class, () -> new GradingPolicyManager(null));
        }

        @Test
        void emptyPolicyIsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new GradingPolicyManager(Collections.<GradeBand>emptyList()));
        }

        @Test
        void policyContainingNullBandIsRejected() {
            List<GradeBand> bands = Arrays.asList(new GradeBand(0.0, g("F", 0.0)), null);

            assertThrows(NullPointerException.class, () -> new GradingPolicyManager(bands));
        }

        @Test
        void incompletePolicyWithoutZeroBandIsRejected() {
            List<GradeBand> bands = Arrays.asList(
                    new GradeBand(90.0, g("A", 4.0)),
                    new GradeBand(60.0, g("D", 1.0)));

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new GradingPolicyManager(bands));
            assertTrue(error.getMessage().contains("0"));
        }

        @Test
        void policyWithOnlyAHighBandLeavesLowScoresUncovered() {
            List<GradeBand> bands = Collections.singletonList(new GradeBand(50.0, g("P", 2.0)));

            assertThrows(IllegalArgumentException.class, () -> new GradingPolicyManager(bands));
        }

        @Test
        void policyWithDuplicateThresholdsIsRejected() {
            List<GradeBand> bands = Arrays.asList(
                    new GradeBand(90.0, g("A-", 3.7)),
                    new GradeBand(90.0, g("A", 4.0)),
                    new GradeBand(0.0, g("F", 0.0)));

            assertThrows(IllegalArgumentException.class, () -> new GradingPolicyManager(bands));
        }

        @Test
        void policyWithDuplicateLetterIsRejected() {
            List<GradeBand> bands = Arrays.asList(
                    new GradeBand(90.0, g("A", 4.0)),
                    new GradeBand(80.0, g("A", 3.0)),
                    new GradeBand(0.0, g("F", 0.0)));

            assertThrows(IllegalArgumentException.class, () -> new GradingPolicyManager(bands));
        }

        @Test
        void policyWhoseGradePointsIncreaseAsPercentageDropsIsRejected() {
            List<GradeBand> bands = Arrays.asList(
                    new GradeBand(90.0, g("B+", 3.3)),
                    new GradeBand(80.0, g("A-", 3.7)),
                    new GradeBand(0.0, g("F", 0.0)));

            assertThrows(IllegalArgumentException.class, () -> new GradingPolicyManager(bands));
        }

        @Test
        void policyAllowingEqualPointsInNeighbouringBandsIsAccepted() {
            List<GradeBand> bands = Arrays.asList(
                    new GradeBand(90.0, g("A", 4.0)),
                    new GradeBand(80.0, g("A-", 4.0)),
                    new GradeBand(0.0, g("F", 0.0)));

            assertEquals("A-", new GradingPolicyManager(bands).getGradeForPercentage(85).getLetter());
        }
    }

    @Nested
    @DisplayName("Policy table behaviour")
    class TableBehaviour {

        @Test
        void plusMinusScaleContainsTheNewMinusAndPlusGrades() {
            List<String> letters = new ArrayList<>();
            for (GradeBand band : policy.getGradeBands()) {
                letters.add(band.getGrade().getLetter());
            }

            assertTrue(letters.containsAll(Arrays.asList("A-", "B+", "C-")));
            assertEquals(11, letters.size());
        }

        @Test
        void bandsAreOrderedFromHighestThresholdToLowest() {
            List<GradeBand> bands = policy.getGradeBands();

            for (int i = 1; i < bands.size(); i++) {
                assertTrue(bands.get(i - 1).getMinimumPercentage() > bands.get(i).getMinimumPercentage());
                assertTrue(bands.get(i - 1).getGrade().getPoints() >= bands.get(i).getGrade().getPoints());
            }
        }

        @Test
        void policyTableCanBeGivenInAnyOrder() {
            List<GradeBand> unordered = Arrays.asList(
                    new GradeBand(0.0, g("F", 0.0)),
                    new GradeBand(90.0, g("A", 4.0)),
                    new GradeBand(50.0, g("C", 2.0)));

            GradingPolicyManager custom = new GradingPolicyManager(unordered);

            assertEquals("A", custom.getGradeForPercentage(95).getLetter());
            assertEquals("C", custom.getGradeForPercentage(50).getLetter());
            assertEquals("F", custom.getGradeForPercentage(Math.nextDown(50.0)).getLetter());
        }

        @Test
        void exposedBandListCannotBeModified() {
            List<GradeBand> bands = policy.getGradeBands();

            assertThrows(UnsupportedOperationException.class, bands::clear);
        }

        @Test
        void changingTheSourceListAfterConstructionDoesNotAffectThePolicy() {
            List<GradeBand> source = new ArrayList<>(Arrays.asList(
                    new GradeBand(50.0, g("P", 2.0)),
                    new GradeBand(0.0, g("F", 0.0))));
            GradingPolicyManager custom = new GradingPolicyManager(source);

            source.clear();

            assertEquals("P", custom.getGradeForPercentage(75).getLetter());
        }
    }

    @Nested
    @DisplayName("Course results")
    class CourseResults {

        @Test
        void createCourseResultKeepsLetterGradeAndPointsInSync() {
            Course course = new Course("CS101", "Programming", 3);

            CourseResult result = policy.createCourseResult(course, 90.0);

            assertEquals("A-", result.getGrade().getLetter());
            assertEquals(3.7, result.getGrade().getPoints(), 0.0001);
            assertEquals(90.0, result.getPercentage(), 0.0001);
        }

        @Test
        void createCourseResultRejectsNullCourse() {
            assertThrows(NullPointerException.class, () -> policy.createCourseResult(null, 85));
        }
    }
}
