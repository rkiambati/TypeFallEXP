package org.example;


public interface TypingTarget {

    String getTargetWord();

    int getTypedCharacterIndex();

    void onCorrectCharacterTyped(int characterIndex);

    boolean isTargetActive();

    boolean isTypedComplete();

    void markAsResolved();

    double getX();

    double getY();
}