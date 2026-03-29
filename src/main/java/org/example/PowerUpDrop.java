package org.example;

/**
 * A falling typeable target that rewards the player with a power-up when typed.
 */
public class PowerUpDrop extends FallingEntity implements TypingTarget {

    private static final double FALL_SPEED = 95.0;

    private final String targetWord;
    private int typedCharacterIndex;
    private boolean targetActive;
    private final PowerUpType powerUpType;

    public PowerUpDrop(String hardWord, PowerUpType powerUpType) {
        super(FALL_SPEED);

        if (hardWord == null || hardWord.trim().isEmpty()) {
            throw new IllegalArgumentException("Power-up word cannot be null or empty.");
        }

        this.targetWord = hardWord;
        this.typedCharacterIndex = 0;
        this.targetActive = true;
        this.powerUpType = powerUpType;
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

    public PowerUpType getPowerUpType() {
        return powerUpType;
    }
}