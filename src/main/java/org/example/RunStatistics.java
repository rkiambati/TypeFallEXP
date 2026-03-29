package org.example;

/**
 * Captures and calculates player performance metrics for a single typing session.
 */
public class RunStatistics {

    public static final double SECONDS_PER_MINUTE = 60.0;
    private static final int CHARS_PER_STANDARD_WORD = 5;

    private int correctChars;
    private int totalChars;
    private int wordsCompleted;
    private double timeElapsed;

    public RunStatistics() {
        this.correctChars = 0;
        this.totalChars = 0;
        this.wordsCompleted = 0;
        this.timeElapsed = 0.0;
    }

    // Input Recorders

    public void recordCorrectCharacterTyped() {
        correctChars++;
        totalChars++;
    }

    public void recordIncorrectCharacterTyped() {
        totalChars++;
    }

    public void recordWordCompleted() {
        wordsCompleted++;
    }

    /**
     * Updates the clock. Call this every frame from the game loop.
     * @param deltaTime The time in seconds since the last frame.
     */
    public void addElapsedTime(double deltaTime) {
        if (deltaTime > 0) {
            this.timeElapsed += deltaTime;
        }
    }

    // Calculated Metrics

    /**
     * Standard WPM uses the formula:
     * $$WPM = \frac{(\text{Correct Characters} / 5)}{\text{Time in Minutes}}$$
     */
    public double calculateWordsPerMinute() {
        if (timeElapsed <= 0 || !hasTypedAnyCharacters()) return 0.0;

        double minutes = timeElapsed / SECONDS_PER_MINUTE;
        return (correctChars / (double) CHARS_PER_STANDARD_WORD) / minutes;
    }

    public double calculateAccuracyPercentage() {
        if (!hasTypedAnyCharacters()) {
            return 100.0; // Assume perfect until a mistake happens
        }
        return ((double) correctChars / totalChars) * 100.0;
    }

    /**
     * Derived logic: Mistakes are the gap between total attempts and correct hits.
     */
    public int getMistakeCount() {
        return totalChars - correctChars;
    }

    // Helpers and Getters

    private boolean hasTypedAnyCharacters() {
        return totalChars > 0;
    }

    public int getCorrectChars() { return correctChars; }
    public int getTotalChars() { return totalChars; }
    public int getWordsCompleted() { return wordsCompleted; }
    public double getTimeElapsed() { return timeElapsed; }
}