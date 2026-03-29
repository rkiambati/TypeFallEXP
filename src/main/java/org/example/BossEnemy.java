package org.example;

/**
 * Stationary boss target that must be typed before the timer expires.
 */
public class BossEnemy extends Enemy {

    private static final int DAMAGE = 3;
    private static final int POINTS = 1000;
    private final double timeLimit;

    public BossEnemy(String paragraph, double timeLimit) {
        super(paragraph, 0.0, DAMAGE, POINTS);
        this.timeLimit = timeLimit;
    }

    public double getTimeLimit() {
        return timeLimit;
    }
}