package org.example;

import org.example.tdd.gpasystem.GradeBand;
import org.example.tdd.gpasystem.GradingPolicyFactory;
import org.example.tdd.gpasystem.GradingPolicyManager;

public class Main {
    public static void main(String[] args) {
        GradingPolicyManager policy = GradingPolicyFactory.plusMinusScale();

        System.out.println("Plus/minus grading table:");
        for (GradeBand band : policy.getGradeBands()) {
            System.out.println("  " + band);
        }

        double[] samples = {100, 93, 92.99, 90, 87, 70, 69.99, 59.99};
        System.out.println("\nSample percentages:");
        for (double percentage : samples) {
            System.out.println("  " + percentage + "% -> " + policy.getGradeForPercentage(percentage).getLetter());
        }
    }
}
