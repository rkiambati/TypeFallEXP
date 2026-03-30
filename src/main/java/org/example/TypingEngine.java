package org.example;

import java.util.List;


public class TypingEngine {

    private TypingTarget currentTypingTarget;

    public TypingEngine() {
        this.currentTypingTarget = null;
    }

    public TypingResult processTypedCharacter(char typedCharacter, List<TypingTarget> activeTargets, RunStatistics stats) {
        char normalizedChar = Character.toLowerCase(typedCharacter);

        // Scenario 1: We are already locked onto a target
        if (currentTypingTarget != null) {
            String word = currentTypingTarget.getTargetWord();
            int index = currentTypingTarget.getTypedCharacterIndex();

            if (index < word.length() && Character.toLowerCase(word.charAt(index)) == normalizedChar) {
                // The character matches the next letter of the locked word
                currentTypingTarget.onCorrectCharacterTyped(index + 1);
                if (stats != null) stats.recordCorrectCharacterTyped();

                if (currentTypingTarget.isTypedComplete()) {
                    currentTypingTarget.markAsResolved();
                    currentTypingTarget = null;
                    return TypingResult.TARGET_COMPLETED;
                }
                return TypingResult.CORRECT_PROGRESS;
            } else {
                // The character is wrong for the current word, check if the player is trying to auto-switch to a new word.
                TypingTarget newTarget = findBestNewTarget(normalizedChar, activeTargets, currentTypingTarget);

                if (newTarget != null) {
                    // Switch to new target, reset the old target and lock the new one.
                    currentTypingTarget.onCorrectCharacterTyped(0);
                    currentTypingTarget = newTarget;
                    currentTypingTarget.onCorrectCharacterTyped(1);

                    if (stats != null) {
                        stats.recordCorrectCharacterTyped();
                    }
                    return TypingResult.CORRECT_PROGRESS;
                } else {
                    // It is just a normal typo, record the mistake, but dont drop the lock, allow the player
                    // to keep typing the word without starting over.
                    if (stats != null) {
                        stats.recordIncorrectCharacterTyped();
                    }
                    return TypingResult.INCORRECT;
                }
            }
        }

        // Scenario 2: We are not locked onto a target, try to find a new target
        TypingTarget newTarget = findBestNewTarget(normalizedChar, activeTargets, null);

        if (newTarget != null) {
            currentTypingTarget = newTarget;
            currentTypingTarget.onCorrectCharacterTyped(1);
            if (stats != null) stats.recordCorrectCharacterTyped();

            if (currentTypingTarget.isTypedComplete()) {
                currentTypingTarget.markAsResolved();
                currentTypingTarget = null;
                return TypingResult.TARGET_COMPLETED;
            }
            return TypingResult.CORRECT_PROGRESS;
        }

        // No match found at all
        if (stats != null) stats.recordIncorrectCharacterTyped();
        return TypingResult.NO_MATCH;
    }

    private TypingTarget findBestNewTarget(char firstChar, List<TypingTarget> activeTargets, TypingTarget excludeTarget) {
        TypingTarget bestMatch = null;
        double lowestY = -1.0;

        for (TypingTarget target : activeTargets) {
            if (target != excludeTarget && target.isTargetActive() && !target.getTargetWord().isEmpty()) {
                if (Character.toLowerCase(target.getTargetWord().charAt(0)) == firstChar) {
                    if (bestMatch == null || target.getY() > lowestY) {
                        bestMatch = target;
                        lowestY = target.getY();
                    }
                }
            }
        }
        return bestMatch;
    }

    public void abandonCurrentTarget() {
        if (currentTypingTarget != null) {
            currentTypingTarget.onCorrectCharacterTyped(0);
            currentTypingTarget = null;
        }
    }

    public void clearFocusIfTargetRemoved(TypingTarget target) {
        if (currentTypingTarget == target) {
            currentTypingTarget = null;
        }
    }

    public void resetFocus() {
        currentTypingTarget = null;
    }
}