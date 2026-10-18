package org.example.tdd.gpasystem;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Question 1: the grading policy factory builds policies from a defined
 * percentage-to-grade table.
 */
class GradingPolicyFactoryTest {

    @Test
    void plusMinusScaleHasElevenBandsFromAToF() {
        GradingPolicyManager policy = GradingPolicyFactory.plusMinusScale();

        assertEquals(11, policy.getGradeBands().size());
        assertEquals("A", policy.getGradeBands().get(0).getGrade().getLetter());
        assertEquals("F", policy.getGradeBands().get(10).getGrade().getLetter());
    }

    @Test
    void plusMinusScaleReturnsANewIndependentPolicyEachTime() {
        assertEquals(GradingPolicyFactory.plusMinusScale().getGradeBands(),
                GradingPolicyFactory.plusMinusScale().getGradeBands());
    }

    @Test
    void standardScaleHasFiveBands() {
        GradingPolicyManager policy = GradingPolicyFactory.standardFourPointScale();

        assertEquals(5, policy.getGradeBands().size());
        assertEquals("A", policy.getGradeForPercentage(90).getLetter());
        assertEquals("B", policy.getGradeForPercentage(Math.nextDown(90.0)).getLetter());
        assertEquals("F", policy.getGradeForPercentage(Math.nextDown(60.0)).getLetter());
    }

    @Test
    void managerFactoryMethodsDelegateToTheFactory() {
        assertEquals(GradingPolicyFactory.plusMinusScale().getGradeBands(),
                GradingPolicyManager.plusMinusScale().getGradeBands());
        assertEquals(GradingPolicyFactory.standardFourPointScale().getGradeBands(),
                GradingPolicyManager.standardFourPointScale().getGradeBands());
    }

    @Test
    void fromTableBuildsAPolicyFromPercentageToGradeEntries() {
        Map<Double, Grade> table = new LinkedHashMap<>();
        table.put(50.0, new Grade("P", 2.0));
        table.put(0.0, new Grade("F", 0.0));

        GradingPolicyManager policy = GradingPolicyFactory.fromTable(table);

        assertEquals("P", policy.getGradeForPercentage(50).getLetter());
        assertEquals("F", policy.getGradeForPercentage(Math.nextDown(50.0)).getLetter());
    }

    @Test
    void fromTableRejectsNullTable() {
        assertThrows(NullPointerException.class, () -> GradingPolicyFactory.fromTable(null));
    }

    @Test
    void fromTableRejectsEmptyTable() {
        assertThrows(IllegalArgumentException.class,
                () -> GradingPolicyFactory.fromTable(new HashMap<Double, Grade>()));
    }

    @Test
    void fromTableRejectsIncompleteTableWithoutZeroBand() {
        Map<Double, Grade> table = new HashMap<>();
        table.put(90.0, new Grade("A", 4.0));
        table.put(50.0, new Grade("C", 2.0));

        assertThrows(IllegalArgumentException.class, () -> GradingPolicyFactory.fromTable(table));
    }

    @Test
    void fromTableRejectsNullPercentageNullGradeAndOutOfRangeCutoff() {
        Map<Double, Grade> nullKey = new HashMap<>();
        nullKey.put(null, new Grade("A", 4.0));
        assertThrows(NullPointerException.class, () -> GradingPolicyFactory.fromTable(nullKey));

        Map<Double, Grade> nullGrade = new HashMap<>();
        nullGrade.put(0.0, null);
        assertThrows(NullPointerException.class, () -> GradingPolicyFactory.fromTable(nullGrade));

        Map<Double, Grade> outOfRange = new HashMap<>();
        outOfRange.put(0.0, new Grade("F", 0.0));
        outOfRange.put(101.0, new Grade("A", 4.0));
        assertThrows(IllegalArgumentException.class, () -> GradingPolicyFactory.fromTable(outOfRange));
    }

    @Test
    void fromTableRejectsGradePointsThatIncreaseAsPercentageDrops() {
        Map<Double, Grade> table = new HashMap<>();
        table.put(90.0, new Grade("B", 3.0));
        table.put(50.0, new Grade("A", 4.0));
        table.put(0.0, new Grade("F", 0.0));

        assertThrows(IllegalArgumentException.class, () -> GradingPolicyFactory.fromTable(table));
    }
}
