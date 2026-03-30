package org.example;

public class StandardEnemy extends Enemy {

    private static final double MOVEMENT_SPEED = 120.0;
    private static final int DAMAGE = 1;
    private static final int POINTS = 100;

    public StandardEnemy(String targetWord) {
        super(targetWord, MOVEMENT_SPEED, DAMAGE, POINTS);
    }
}