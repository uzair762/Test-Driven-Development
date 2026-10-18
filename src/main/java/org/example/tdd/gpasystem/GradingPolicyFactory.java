package org.example.tdd.gpasystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Factory that builds {@link GradingPolicyManager}s from a defined
 * percentage-to-grade table.
 *
 * <p>The plus/minus table used by the university:
 *
 * <pre>
 *  93-100 A  4.0     77-79 C+ 2.3
 *  90-92  A- 3.7     73-76 C  2.0
 *  87-89  B+ 3.3     70-72 C- 1.7
 *  83-86  B  3.0     67-69 D+ 1.3
 *  80-82  B- 2.7     60-66 D  1.0
 *                    0-59  F  0.0
 * </pre>
 */
public final class GradingPolicyFactory {

    private GradingPolicyFactory() {
    }

    /** Original policy: A, B, C, D, F. */
    public static GradingPolicyManager standardFourPointScale() {
        return new GradingPolicyManager(Arrays.asList(
                new GradeBand(90.0, new Grade("A", 4.0)),
                new GradeBand(80.0, new Grade("B", 3.0)),
                new GradeBand(70.0, new Grade("C", 2.0)),
                new GradeBand(60.0, new Grade("D", 1.0)),
                new GradeBand(0.0, new Grade("F", 0.0))));
    }

    /** Plus/minus policy with A-, B+, C- and the other intermediate grades. */
    public static GradingPolicyManager plusMinusScale() {
        return new GradingPolicyManager(Arrays.asList(
                new GradeBand(93.0, new Grade("A", 4.0)),
                new GradeBand(90.0, new Grade("A-", 3.7)),
                new GradeBand(87.0, new Grade("B+", 3.3)),
                new GradeBand(83.0, new Grade("B", 3.0)),
                new GradeBand(80.0, new Grade("B-", 2.7)),
                new GradeBand(77.0, new Grade("C+", 2.3)),
                new GradeBand(73.0, new Grade("C", 2.0)),
                new GradeBand(70.0, new Grade("C-", 1.7)),
                new GradeBand(67.0, new Grade("D+", 1.3)),
                new GradeBand(60.0, new Grade("D", 1.0)),
                new GradeBand(0.0, new Grade("F", 0.0))));
    }

    /**
     * Builds a policy from a table of "minimum percentage -> grade" entries.
     * The same validation as {@link GradingPolicyManager} applies, so an
     * incomplete or inconsistent table is rejected.
     */
    public static GradingPolicyManager fromTable(Map<Double, Grade> table) {
        Objects.requireNonNull(table, "table must not be null");
        List<GradeBand> bands = new ArrayList<>();
        for (Map.Entry<Double, Grade> entry : table.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "table must not contain a null percentage");
            bands.add(new GradeBand(entry.getKey(), entry.getValue()));
        }
        return new GradingPolicyManager(bands);
    }
}
