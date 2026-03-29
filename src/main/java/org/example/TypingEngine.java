package org.example;

import java.util.Comparator;
import java.util.List;

/**
 * Handles target locking and typing logic.
 */
public class TypingEngine {

    private TypingTarget currentTypingTarget;

    public TypingEngine() {
        this.currentTypingTarget = null;
    }

    public TypingResult processTypedCharacter(char typedCharacter, List<TypingTarget> activeTargets, RunStatistics stats) {
        char normalizedCharacter = Character.toLowerCase(typedCharacter);

        if (currentTypingTarget == null) {
            return findAndLockNewTarget(normalizedCharacter, activeTargets, stats);
        }

        return processLockedTarget(normalizedCharacter, stats);
    }

    private TypingResult findAndLockNewTarget(char typedChar, List<TypingTarget> activeTargets, RunStatistics stats) {
        TypingTarget bestMatch = null;
        double lowestY = -1.0;

        for (TypingTarget target : activeTargets) {
            if (target.isTargetActive() && !target.getTargetWord().isEmpty()) {
                char firstChar = Character.toLowerCase(target.getTargetWord().charAt(0));
                if (firstChar == typedChar) {
                    // Find the target closest to the bottom (highest Y value)
                    if (bestMatch == null || target.getY() > lowestY) {
                        bestMatch = target;
                        lowestY = target.getY();
                    }
                }
            }
        }

        if (bestMatch == null) {
            if (stats != null) {
                stats.recordIncorrectCharacterTyped();
            }
            return TypingResult.NO_MATCH;
        }

        currentTypingTarget = bestMatch;
        currentTypingTarget.onCorrectCharacterTyped(1);

        if (stats != null) {
            stats.recordCorrectCharacterTyped();
        }

        if (currentTypingTarget.isTypedComplete()) {
            currentTypingTarget.markAsResolved();
            currentTypingTarget = null;
            return TypingResult.TARGET_COMPLETED;
        }

        return TypingResult.CORRECT_PROGRESS;
    }

    private TypingResult processLockedTarget(char typedChar, RunStatistics stats) {
        String word = currentTypingTarget.getTargetWord();
        int index = currentTypingTarget.getTypedCharacterIndex();

        if (index >= word.length()) {
            currentTypingTarget.markAsResolved();
            currentTypingTarget = null;
            return TypingResult.TARGET_COMPLETED;
        }

        char expectedChar = Character.toLowerCase(word.charAt(index));

        if (expectedChar != typedChar) {
            if (stats != null) {
                stats.recordIncorrectCharacterTyped();
            }
            return TypingResult.INCORRECT;
        }

        currentTypingTarget.onCorrectCharacterTyped(index + 1);

        if (stats != null) {
            stats.recordCorrectCharacterTyped();
        }

        if (currentTypingTarget.isTypedComplete()) {
            currentTypingTarget.markAsResolved();
            currentTypingTarget = null;
            return TypingResult.TARGET_COMPLETED;
        }

        return TypingResult.CORRECT_PROGRESS;
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