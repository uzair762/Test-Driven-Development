package org.example.tdd.gpasystem;

import java.util.Objects;

public final class Grade {
    private final String letter;
    private final double points;

    public Grade(String letter, double points) {
        if (letter == null || letter.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade letter must not be blank");
        }
        if (!Double.isFinite(points) || points < 0.0 || points > 4.0) {
            throw new IllegalArgumentException("Grade points must be between 0 and 4");
        }
        this.letter = letter.trim();
        this.points = points;
    }

    public String getLetter() {
        return letter;
    }

    public double getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grade)) {
            return false;
        }
        Grade grade = (Grade) other;
        return Double.compare(grade.points, points) == 0 && letter.equals(grade.letter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(letter, points);
    }
}
