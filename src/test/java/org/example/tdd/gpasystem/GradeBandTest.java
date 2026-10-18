package org.example.tdd.gpasystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Question 1: GradeBand is one row of the percentage-to-grade table.
 */
class GradeBandTest {

    private final Grade aMinus = new Grade("A-", 3.7);

    @Test
    void storesMinimumPercentageAndGrade() {
        GradeBand band = new GradeBand(90.0, aMinus);

        assertEquals(90.0, band.getMinimumPercentage(), 0.0001);
        assertEquals(aMinus, band.getGrade());
    }

    @Test
    void allowsBoundaryThresholdsZeroAndOneHundred() {
        assertEquals(0.0, new GradeBand(0.0, aMinus).getMinimumPercentage(), 0.0001);
        assertEquals(100.0, new GradeBand(100.0, aMinus).getMinimumPercentage(), 0.0001);
    }

    @ParameterizedTest(name = "minimum {0} is rejected")
    @ValueSource(doubles = {-0.01, 100.01, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsThresholdOutsideZeroToOneHundred(double minimum) {
        assertThrows(IllegalArgumentException.class, () -> new GradeBand(minimum, aMinus));
    }

    @Test
    void rejectsNullGrade() {
        assertThrows(NullPointerException.class, () -> new GradeBand(50.0, null));
    }

    @Test
    void bandsWithSameThresholdAndGradeAreEqual() {
        GradeBand first = new GradeBand(90.0, new Grade("A-", 3.7));
        GradeBand second = new GradeBand(90.0, new Grade("A-", 3.7));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void bandsWithDifferentThresholdOrGradeAreNotEqual() {
        GradeBand band = new GradeBand(90.0, aMinus);

        assertNotEquals(band, new GradeBand(91.0, aMinus));
        assertNotEquals(band, new GradeBand(90.0, new Grade("A", 4.0)));
    }
}
