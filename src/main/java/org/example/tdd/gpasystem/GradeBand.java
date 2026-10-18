package org.example.tdd.gpasystem;

import java.util.Objects;

/**
 * One row of a grading table: any percentage greater than or equal to
 * {@code minimumPercentage} (and below the next higher band) earns {@code grade}.
 */
public final class GradeBand {
    private final double minimumPercentage;
    private final Grade grade;

    public GradeBand(double minimumPercentage, Grade grade) {
        if (!Double.isFinite(minimumPercentage) || minimumPercentage < 0.0 || minimumPercentage > 100.0) {
            throw new IllegalArgumentException("Minimum percentage must be between 0 and 100");
        }
        this.minimumPercentage = minimumPercentage;
        this.grade = Objects.requireNonNull(grade, "grade must not be null");
    }

    public double getMinimumPercentage() {
        return minimumPercentage;
    }

    public Grade getGrade() {
        return grade;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GradeBand)) {
            return false;
        }
        GradeBand band = (GradeBand) other;
        return Double.compare(band.minimumPercentage, minimumPercentage) == 0
                && grade.equals(band.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minimumPercentage, grade);
    }

    @Override
    public String toString() {
        return minimumPercentage + "% -> " + grade.getLetter() + " (" + grade.getPoints() + ")";
    }
}
