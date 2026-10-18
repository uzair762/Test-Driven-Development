package org.example.tdd.gpasystem;

import java.util.Locale;

public final class Course {
    private final String code;
    private final String title;
    private final double credits;

    public Course(String code, String title, double credits) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code must not be blank");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title must not be blank");
        }
        if (!Double.isFinite(credits) || credits <= 0.0) {
            throw new IllegalArgumentException("Course credits must be greater than zero");
        }
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.title = title.trim();
        this.credits = credits;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public double getCredits() {
        return credits;
    }
}
