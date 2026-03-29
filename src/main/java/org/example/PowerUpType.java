package org.example;

public enum PowerUpType {
    SCREEN_CLEAR("Screen Clear"),
    HEAL("+10 Health");

    private final String displayName;

    PowerUpType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}