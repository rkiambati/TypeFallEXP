package org.example;

/**
 * Represents anything on screen that can be typed by the player.
 */
public interface TypingTarget {

    /**
     * Returns the full text the player must type.
     */
    String getTargetWord();

    /**
     * Returns how many characters have been typed correctly so far.
     */
    int getTypedCharacterIndex();

    /**
     * Updates progress after a correct character is typed.
     *
     * @param characterIndex the next character index to expect
     */
    void onCorrectCharacterTyped(int characterIndex);

    /**
     * Returns whether this target is still active.
     *
     * @return true if active, false otherwise
     */
    boolean isTargetActive();

    /**
     * Returns whether the full word has been typed.
     *
     * @return true if typing is complete
     */
    boolean isTypedComplete();

    /**
     * Marks this target as resolved and inactive.
     * This works for both enemies and power-up drops.
     */
    void markAsResolved();

    /**
     * Returns the X position on screen.
     */
    double getX();

    /**
     * Returns the Y position on screen.
     */
    double getY();
}