package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameSession {

    public static final int DEFAULT_MAXIMUM_HEARTS = 100;

    private static final double STANDARD_ENEMY_SPAWN_INTERVAL = 2.0;
    private static final double POWER_UP_SPAWN_INTERVAL = 12.0;
    private static final double BOSS_INTERVAL_SECONDS = 30.0;
    private static final double PLAYFIELD_HEIGHT = 540;

    private static final double BOSS_WAVE_SPAWN_INTERVAL = 0.5; // Spawns an enemy every half second

    private int bossWaveEnemiesToSpawn;
    private int bossWaveEnemiesDefeated;
    private int totalBossWaveEnemies;

    private int currentScore;
    private int currentHealth;
    private int currentLevel;

    private int enemiesDefeatedThisLevel;
    private int enemiesRequiredForBoss;

    private boolean bossActive;
    private boolean bossDefeated;

    private double enemySpawnTimer;
    private double powerUpSpawnTimer;
    private double bossTimer;

    private int screenClearCharges;
    private int healCharges;

    private final List<TypingTarget> activeTargets;
    private final TypingEngine typingEngine;
    private final RunStatistics runStatistics;
    private final WordGenerator wordGenerator;
    private final Random random;

    public GameSession(int startingLevel) {
        this.wordGenerator = new WordGenerator("/words.txt");
        this.currentHealth = DEFAULT_MAXIMUM_HEARTS;
        this.currentScore = 0;
        this.activeTargets = new ArrayList<>();
        this.typingEngine = new TypingEngine();
        this.runStatistics = new RunStatistics();
        this.random = new Random();

        startLevel(startingLevel);
    }

    public void startLevel(int level) {
        this.currentLevel = level;
        this.activeTargets.clear();
        this.enemiesDefeatedThisLevel = 0;
        this.enemiesRequiredForBoss = 5 + (level * 5);

        this.totalBossWaveEnemies = 20 + (level * 10); // Increases wave size each level
        this.bossWaveEnemiesToSpawn = 0;
        this.bossWaveEnemiesDefeated = 0;

        this.bossActive = false;
        this.bossDefeated = false;

        this.enemySpawnTimer = STANDARD_ENEMY_SPAWN_INTERVAL;
        this.powerUpSpawnTimer = POWER_UP_SPAWN_INTERVAL;

        typingEngine.resetFocus();
    }

    public void update(double deltaTimeSeconds) {
        if (isGameOver() || isLevelCleared()) return;

        runStatistics.addElapsedTime(deltaTimeSeconds);
        spawnTargets(deltaTimeSeconds);
        updateTargetMovement(deltaTimeSeconds);
        resolveCompletedTargets();
    }

    private void updateBossTimer(double deltaTimeSeconds) {
        if (!bossActive) return;

        bossTimer -= deltaTimeSeconds;
        if (bossTimer <= 0.0) {
            currentHealth = 0;
        }
    }

    private void spawnTargets(double deltaTimeSeconds) {
        enemySpawnTimer -= deltaTimeSeconds;

        if (enemySpawnTimer <= 0.0) {
            if (bossActive && bossWaveEnemiesToSpawn > 0) {
                enemySpawnTimer = BOSS_WAVE_SPAWN_INTERVAL;
                bossWaveEnemiesToSpawn--;
                spawnStandardEnemy();
            } else if (!bossActive) {
                enemySpawnTimer = STANDARD_ENEMY_SPAWN_INTERVAL;
                spawnStandardEnemy();
            }
        }

        powerUpSpawnTimer -= deltaTimeSeconds;
        if (powerUpSpawnTimer <= 0.0) {
            powerUpSpawnTimer = POWER_UP_SPAWN_INTERVAL;
            spawnPowerUpDrop();
        }
    }

    private void spawnStandardEnemy() {
        StandardEnemy newEnemy = new StandardEnemy(wordGenerator.getRandomWord());
        newEnemy.setX(random.nextDouble() * 1100.0);
        activeTargets.add(newEnemy);
    }

    private void spawnPowerUpDrop() {
        PowerUpType type = random.nextBoolean() ? PowerUpType.SCREEN_CLEAR : PowerUpType.HEAL;
        PowerUpDrop drop = new PowerUpDrop(wordGenerator.getRandomHardWord(), type);
        drop.setX(random.nextDouble() * 1080.0);
        activeTargets.add(drop);
    }

    private void updateTargetMovement(double deltaTimeSeconds) {
        for (int i = activeTargets.size() - 1; i >= 0; i--) {
            TypingTarget target = activeTargets.get(i);

            if (target instanceof FallingEntity fallingEntity) {
                fallingEntity.moveDown(deltaTimeSeconds);

                if (fallingEntity.hasReachedBottom(PLAYFIELD_HEIGHT)) {
                    if (target instanceof Enemy enemy) {
                        currentHealth -= enemy.getDamageToPlayerHearts();
                        processEnemyRemoval(); // Track the enemy even if it hit us
                    }
                    typingEngine.clearFocusIfTargetRemoved(target);
                    activeTargets.remove(i);
                }
            }
        }
    }

    private void resolveCompletedTargets() {
        for (int i = activeTargets.size() - 1; i >= 0; i--) {
            TypingTarget target = activeTargets.get(i);

            if (!target.isTargetActive()) {
                activeTargets.remove(i);
                typingEngine.clearFocusIfTargetRemoved(target);

                runStatistics.recordWordCompleted();

                if (target instanceof Enemy defeatedEnemy) {
                    currentScore += defeatedEnemy.getPointsAwardedOnDefeat();
                    processEnemyRemoval(); // Track the defeated enemy
                } else if (target instanceof PowerUpDrop powerUpDrop) {
                    grantPowerUp(powerUpDrop.getPowerUpType());
                }
            }
        }
    }

    private void checkAndSpawnBoss() {
        if (enemiesDefeatedThisLevel < enemiesRequiredForBoss || bossActive) return;

        bossActive = true;
        bossWaveEnemiesToSpawn = totalBossWaveEnemies;
        bossWaveEnemiesDefeated = 0;

        // Optional: clear standard enemies before the wave hits, or leave them for extra chaos
        // activeTargets.clear();
        typingEngine.resetFocus();
    }

    private void processEnemyRemoval() {
        if (bossActive) {
            bossWaveEnemiesDefeated++;
            if (bossWaveEnemiesDefeated >= totalBossWaveEnemies && bossWaveEnemiesToSpawn == 0) {
                bossDefeated = true;
            }
        } else {
            enemiesDefeatedThisLevel++;
            checkAndSpawnBoss();
        }
    }

    public void abandonCurrentTarget() {
        typingEngine.abandonCurrentTarget();
    }


    public void handleTypedCharacter(char input) {
        if (isGameOver() || isLevelCleared()) return;

        TypingResult result = typingEngine.processTypedCharacter(input, activeTargets, runStatistics);

        if (result == TypingResult.INCORRECT || result == TypingResult.NO_MATCH) {
            currentHealth = Math.max(0, currentHealth - 1);
        }
    }

    private void grantPowerUp(PowerUpType powerUpType) {
        if (powerUpType == PowerUpType.SCREEN_CLEAR) {
            screenClearCharges++;
        } else if (powerUpType == PowerUpType.HEAL) {
            healCharges++;
        }
    }

    public boolean useScreenClearPowerUp() {
        if (screenClearCharges <= 0) return false;

        screenClearCharges--;
        for (int i = activeTargets.size() - 1; i >= 0; i--) {
            TypingTarget target = activeTargets.get(i);
            if (target instanceof StandardEnemy) {
                typingEngine.clearFocusIfTargetRemoved(target);
                activeTargets.remove(i);
            }
        }
        return true;
    }

    public boolean useHealPowerUp() {
        if (healCharges <= 0) return false;

        healCharges--;
        currentHealth += 10;
        return true;
    }

    public boolean isGameOver() {
        return currentHealth <= 0;
    }

    public boolean isLevelCleared() {
        return bossDefeated;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public RunStatistics getRunStatistics() {
        return runStatistics;
    }

    public List<TypingTarget> getActiveTargets() {
        return activeTargets;
    }

    public int getScreenClearCharges() {
        return screenClearCharges;
    }

    public int getHealCharges() {
        return healCharges;
    }

    public double getBossTimerRemaining() {
        return bossTimer;
    }

    public boolean isBossActive() {
        return bossActive;
    }
}