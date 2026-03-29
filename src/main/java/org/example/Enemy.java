package org.example;

/**
 * Base enemy type for all hostile typing targets.
 */
public class Enemy extends FallingEntity implements TypingTarget {

    protected String targetWord;
    protected int typedCharacterIndex;
    protected boolean targetActive;

    protected int damageToPlayerHearts;
    protected int pointsAwardedOnDefeat;

    public Enemy(String targetWord, double movementSpeedUnitsPerSecond, int damageToPlayerHearts, int pointsAwardedOnDefeat) {
        super(movementSpeedUnitsPerSecond);
        validateTargetWordNotEmpty(targetWord);

        this.targetWord = targetWord;
        this.typedCharacterIndex = 0;
        this.targetActive = true;
        this.damageToPlayerHearts = damageToPlayerHearts;
        this.pointsAwardedOnDefeat = pointsAwardedOnDefeat;
    }

    @Override
    public String getTargetWord() {
        return targetWord;
    }

    @Override
    public int getTypedCharacterIndex() {
        return typedCharacterIndex;
    }

    @Override
    public void onCorrectCharacterTyped(int characterIndex) {
        this.typedCharacterIndex = Math.max(0, Math.min(characterIndex, targetWord.length()));
    }

    @Override
    public boolean isTargetActive() {
        return targetActive;
    }

    @Override
    public boolean isTypedComplete() {
        return typedCharacterIndex >= targetWord.length();
    }

    @Override
    public void markAsResolved() {
        this.targetActive = false;
    }

    public int getDamageToPlayerHearts() {
        return damageToPlayerHearts;
    }

    public int getPointsAwardedOnDefeat() {
        return pointsAwardedOnDefeat;
    }

    private void validateTargetWordNotEmpty(String targetWord) {
        if (targetWord == null || targetWord.trim().isEmpty()) {
            throw new IllegalArgumentException("Target word cannot be null or empty.");
        }
    }
}