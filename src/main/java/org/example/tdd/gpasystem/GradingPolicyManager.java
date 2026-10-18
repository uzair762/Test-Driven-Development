package org.example.tdd.gpasystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Converts percentages to grades using a table of {@link GradeBand}s.
 *
 * <p>Build policies with {@link GradingPolicyFactory}: {@code standardFourPointScale()}
 * for A-F, {@code plusMinusScale()} for A-, B+, C- and friends, or {@code fromTable(...)}
 * for a custom percentage-to-grade table.
 */
public final class GradingPolicyManager {
    private final List<GradeBand> gradeBands;

    public GradingPolicyManager(List<GradeBand> gradeBands) {
        Objects.requireNonNull(gradeBands, "gradeBands must not be null");
        if (gradeBands.isEmpty()) {
            throw new IllegalArgumentException("At least one grade band is required");
        }
        List<GradeBand> copy = new ArrayList<>(gradeBands);
        Set<Double> thresholds = new HashSet<>();
        Set<String> letters = new HashSet<>();
        for (GradeBand band : copy) {
            Objects.requireNonNull(band, "grade bands must not contain null");
            if (!thresholds.add(band.getMinimumPercentage())) {
                throw new IllegalArgumentException("Grade band thresholds must be unique");
            }
            if (!letters.add(band.getGrade().getLetter())) {
                throw new IllegalArgumentException("Grade letters must be unique: "
                        + band.getGrade().getLetter());
            }
        }
        copy.sort(Comparator.comparingDouble(GradeBand::getMinimumPercentage).reversed());
        if (copy.get(copy.size() - 1).getMinimumPercentage() != 0.0) {
            throw new IllegalArgumentException("A grade band starting at 0 is required");
        }
        for (int i = 1; i < copy.size(); i++) {
            double higherPoints = copy.get(i - 1).getGrade().getPoints();
            double lowerPoints = copy.get(i).getGrade().getPoints();
            if (lowerPoints > higherPoints) {
                throw new IllegalArgumentException(
                        "Grade points must not increase as the percentage threshold decreases");
            }
        }
        this.gradeBands = Collections.unmodifiableList(copy);
    }

    /** Original A-F policy; kept for existing callers, see {@link GradingPolicyFactory}. */
    public static GradingPolicyManager standardFourPointScale() {
        return GradingPolicyFactory.standardFourPointScale();
    }

    /** Plus/minus policy; kept for convenience, see {@link GradingPolicyFactory}. */
    public static GradingPolicyManager plusMinusScale() {
        return GradingPolicyFactory.plusMinusScale();
    }

    /** Bands sorted from the highest threshold to the lowest (read-only). */
    public List<GradeBand> getGradeBands() {
        return gradeBands;
    }

    public Grade getGradeForPercentage(double percentage) {
        if (!Double.isFinite(percentage) || percentage < 0.0 || percentage > 100.0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        for (GradeBand band : gradeBands) {
            if (percentage >= band.getMinimumPercentage()) {
                return band.getGrade();
            }
        }
        throw new IllegalStateException("Grading policy does not cover percentage " + percentage);
    }

    public CourseResult createCourseResult(Course course, double percentage) {
        return new CourseResult(course, percentage, getGradeForPercentage(percentage));
    }
}
