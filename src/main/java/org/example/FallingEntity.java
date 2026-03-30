package org.example;


public abstract class FallingEntity {

    protected double xPosition;
    protected double yPosition;
    protected double movementSpeedUnitsPerSecond;

    public FallingEntity(double movementSpeedUnitsPerSecond) {
        this.xPosition = 0.0;
        this.yPosition = 0.0;
        this.movementSpeedUnitsPerSecond = movementSpeedUnitsPerSecond;
    }

    public void moveDown(double deltaTimeSeconds) {
        this.yPosition += movementSpeedUnitsPerSecond * deltaTimeSeconds;
    }

    public boolean hasReachedBottom(double screenHeight) {
        return this.yPosition >= screenHeight;
    }

    public void setX(double xPosition) {
        this.xPosition = xPosition;
    }

    public void setY(double yPosition) {
        this.yPosition = yPosition;
    }

    public double getX() {
        return xPosition;
    }

    public double getY() {
        return yPosition;
    }
}