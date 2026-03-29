package org.example.controller;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.example.*;

import java.io.InputStream;
import java.util.*;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.Account;
import org.example.AccountManager;
import org.example.SceneManager;
import org.example.SessionManager;


public class GameController {

    @FXML
    private Pane backgroundPane;
    @FXML private AnchorPane gamePane;
    @FXML private Text scoreText;
    @FXML private Text healthText;
    @FXML private Text levelText;
    @FXML private Text powerupText;
    @FXML private Text statusText;

    private final Random random = new Random();


    private GameSession gameSession;
    private MainGameLoop gameLoop;
    private long lastFrameTime = 0L;

    private final Map<TypingTarget, TargetView> visualElements = new HashMap<>();
    private final Queue<TargetView> targetViewPool = new LinkedList<>();

    private ImageView playerShip;
    private TypingTarget activeTarget = null;
    private int lastTypedIndex = 0;

    private Image enemyImageCache;
    private Image bossImageCache;
    private Line playerLaser;

    private int laserFramesRemaining = 0;
    private int lastDisplayedScore = -1;
    private int lastDisplayedHealth = -1;
    private int lastDisplayedLevel = -1;
    private String lastDisplayedPowerUp = "";
    private String lastDisplayedStatus = "";

    private boolean wasBossActive = false; // Add this line


    private void spawnAsteroid() {
        // Create the Asteroid using a Circle placeholder
        Circle asteroid = new Circle();

        // Randomize size between 2 and 6 radius
        asteroid.setRadius(random.nextDouble() * 4 + 2);
        asteroid.setFill(Color.web("#FFEE8C"));
        asteroid.setOpacity(random.nextDouble() * 0.5 + 0.3);

        // Set Starting Position:
        // Random X across the 1280px width, Y just off-screen at the bottom (e.g., 750)
        double startX = random.nextDouble() * 1280;
        asteroid.setTranslateX(startX);
        asteroid.setTranslateY(750); // Starts below the screen

        // Add to the background pane and push to the very back
        backgroundPane.getChildren().add(asteroid);
        asteroid.toBack();

        // Create the Animation
        double durationSeconds = random.nextDouble() * 25 + 15; // 15 to 40 seconds
        TranslateTransition transition = new TranslateTransition(Duration.seconds(durationSeconds), asteroid);

        // Move it UP across the screen.
        // Moving from Y=750 to Y=-50 requires a change of -800 pixels.
        transition.setByY(-800);

        // Ensure we don't move it on the X axis anymore
        transition.setByX(0);

        // delay between each asteroid
        transition.setDelay(Duration.seconds(random.nextDouble() * 30));


        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(TranslateTransition.INDEFINITE);

        // Start the movement
        transition.play();
    }


    @FXML
    public void initialize() {
        // Spawn 100 asteroids when the login screen loads
        for (int i = 0; i < 150; i++) {
            spawnAsteroid();
        }

        enemyImageCache = loadImage("/enemy.png");

        setupLaser();

        // Prevent layout thrashing when enemies fall off screen
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(gamePane.widthProperty());
        clip.heightProperty().bind(gamePane.heightProperty());
        gamePane.setClip(clip);

        gameSession = new GameSession(1);
        setupPlayerShip();
        showLevelStartAnimation("Type the enemy words before they hit you");
        gameLoop = new MainGameLoop();
        gameLoop.start();
    }

    private void setupPlayerShip() {
        Image playerImage = loadImage("/player.png");
        playerShip = new ImageView(playerImage);
        playerShip.setFitWidth(150);
        playerShip.setPreserveRatio(true);


        // Use these instead:
        playerShip.setTranslateX(580);
        playerShip.setTranslateY(500);
        gamePane.getChildren().add(playerShip);
        playerShip.toFront();
    }

    private Image loadImage(String path) {
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) return null;
            return new Image(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    private void showLevelStartAnimation(String message) {
        Text readyText = new Text(message);
        readyText.setFill(Color.WHITE);
        readyText.setFont(Font.font("System Bold", 48));
        readyText.setWrappingWidth(1280);
        readyText.setTextAlignment(TextAlignment.CENTER);
        readyText.setLayoutY(300);

        gamePane.getChildren().add(readyText);

        FadeTransition fade = new FadeTransition(Duration.seconds(2), readyText);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(1));
        fade.setOnFinished(e -> gamePane.getChildren().remove(readyText));
        fade.play();
    }

    public void handleKeyPress(char key) {
        gameSession.handleTypedCharacter(key);
    }

    public void usePowerUpSlot(int slotNumber) {
        boolean used = false;
        if (slotNumber == 1) {
            used = gameSession.useScreenClearPowerUp();
        } else if (slotNumber == 2) {
            used = gameSession.useHealPowerUp();
        }

        if (used) {
            showStatusFlash(slotNumber == 1 ? "Screen Cleared" : "+10 Health");
        }
    }

    private void processGameFrame(long now) {
        if (lastFrameTime == 0L) {
            lastFrameTime = now;
            return;
        }

        double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;

        gameSession.update(deltaTime);

        // Check if the boss wave just started
        boolean isBossActiveNow = gameSession.isBossActive();
        if (isBossActiveNow && !wasBossActive) {
            showLevelStartAnimation("MASSIVE WAVE INCOMING!");
        }
        wasBossActive = isBossActiveNow;

        updateHud();
        syncVisuals();

        if (laserFramesRemaining > 0) {
            laserFramesRemaining--;
            if (laserFramesRemaining <= 0) {
                playerLaser.setVisible(false);
            }
        }

        if (gameSession.isGameOver()) {
            gameLoop.stop();
            showStatusFlash("Game Over");
        }

        if (gameSession.isLevelCleared()) {
            int nextLevel = gameSession.getCurrentLevel() + 1;
            // Play big animation instead of tiny flash
            showLevelStartAnimation("WAVE CLEARED!");
            gameSession.startLevel(nextLevel);
            wasBossActive = false; // Reset for the new level
        }
    }

    private void updateHud() {
        if (lastDisplayedScore != gameSession.getCurrentScore()) {
            lastDisplayedScore = gameSession.getCurrentScore();
            scoreText.setText("Score: " + lastDisplayedScore);
        }

        if (lastDisplayedHealth != gameSession.getCurrentHealth()) {
            lastDisplayedHealth = gameSession.getCurrentHealth();
            healthText.setText("Health: " + lastDisplayedHealth);
        }

        if (lastDisplayedLevel != gameSession.getCurrentLevel()) {
            lastDisplayedLevel = gameSession.getCurrentLevel();
            levelText.setText("Level: " + lastDisplayedLevel);
        }

        String currentPowerUpText = "1: Screen Clear x" + gameSession.getScreenClearCharges() +
                "   2: Heal (+10) x" + gameSession.getHealCharges();
        if (!currentPowerUpText.equals(lastDisplayedPowerUp)) {
            lastDisplayedPowerUp = currentPowerUpText;
            powerupText.setText(currentPowerUpText);
        }

        String currentStatusText = " ";
        Color currentStatusColor = Color.WHITE;

        if (gameSession.isBossActive()) {
            currentStatusText = String.format("Boss timer %.0fs", gameSession.getBossTimerRemaining());
            currentStatusColor = Color.ORANGERED;
        }

        if (!currentStatusText.equals(lastDisplayedStatus)) {
            lastDisplayedStatus = currentStatusText;
            statusText.setText(currentStatusText);
            statusText.setFill(currentStatusColor);
        }
    }

    private void syncVisuals() {
        List<TypingTarget> currentTargets = gameSession.getActiveTargets();

        for (TypingTarget target : currentTargets) {
            TargetView view = visualElements.computeIfAbsent(target, this::getOrCreateTargetView);

            // Bypass layout engine, translate directly on GPU
            view.container.setTranslateX(target.getX());
            view.container.setTranslateY(target.getY());

            // ALWAYS update text to catch resets from the TypingEngine
            updateTargetText(view, target);

            if (target.getTypedCharacterIndex() > 0) {
                if (target == activeTarget && target.getTypedCharacterIndex() > lastTypedIndex) {
                    fireLaser(target.getX(), target.getY());
                    lastTypedIndex = target.getTypedCharacterIndex();
                } else if (target != activeTarget) {
                    activeTarget = target;
                    lastTypedIndex = target.getTypedCharacterIndex();
                    fireLaser(target.getX(), target.getY());
                }
            } else if (target == activeTarget && target.getTypedCharacterIndex() == 0) {
                // If the engine reset the index to 0, clear the local lock
                activeTarget = null;
                lastTypedIndex = 0;
            }
        }

        visualElements.entrySet().removeIf(entry -> {
            TypingTarget target = entry.getKey();
            TargetView view = entry.getValue();

            if (!currentTargets.contains(target)) {
                gamePane.getChildren().remove(view.container);
                targetViewPool.offer(view);

                if (target == activeTarget) {
                    activeTarget = null;
                    lastTypedIndex = 0;
                }
                return true;
            }
            return false;
        });
    }

    private TargetView getOrCreateTargetView(TypingTarget target) {
        TargetView view = targetViewPool.poll();

        if (view == null) {
            VBox container = new VBox();
            container.setAlignment(Pos.CENTER);
            container.setSpacing(4);

            Text typedText = new Text();
            Text remainingText = new Text();
            TextFlow textFlow = new TextFlow(typedText, remainingText);
            textFlow.setTextAlignment(TextAlignment.CENTER);

            container.getChildren().addAll(new Rectangle(), textFlow);
            view = new TargetView(container, typedText, remainingText);
        }

        Node headerNode = createVisualNodeForTarget(target);
        view.container.getChildren().set(0, headerNode);

        styleTextForTarget(target, view.typedText, view.remainingText);
        updateTargetText(view, target);

        gamePane.getChildren().add(view.container);
        return view;
    }

    private Node createVisualNodeForTarget(TypingTarget target) {
        if (target instanceof PowerUpDrop powerUpDrop) {
            boolean isClear = powerUpDrop.getPowerUpType() == PowerUpType.SCREEN_CLEAR;
            Text badge = new Text(isClear ? "CLEAR" : "HEAL");
            badge.setFill(isClear ? Color.GOLD : Color.HOTPINK);
            badge.setFont(Font.font("System Bold", 20));
            return badge;
        }

        if (enemyImageCache != null) {
            ImageView imageView = new ImageView(enemyImageCache);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            return imageView;
        }

        Text fallback = new Text("ENEMY");
        fallback.setFill(Color.WHITE);
        fallback.setFont(Font.font("System Bold", 20));
        return fallback;
    }

    public void cancelCurrentTarget() {
        gameSession.abandonCurrentTarget();
    }

    private void styleTextForTarget(TypingTarget target, Text typedText, Text remainingText) {

        if (target instanceof PowerUpDrop powerUpDrop) {
            boolean isClear = powerUpDrop.getPowerUpType() == PowerUpType.SCREEN_CLEAR;
            typedText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            remainingText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            typedText.setFill(Color.LIME);
            remainingText.setFill(isClear ? Color.GOLD : Color.HOTPINK);
            return;
        }

        typedText.setStyle("-fx-font-size: 26px;");
        remainingText.setStyle("-fx-font-size: 26px;");
        typedText.setFill(Color.LIME);
        remainingText.setFill(Color.WHITE);
    }

    private void updateTargetText(TargetView view, TypingTarget target) {
        String fullWord = target.getTargetWord();
        int typedCount = Math.max(0, Math.min(target.getTypedCharacterIndex(), fullWord.length()));

        view.typedText.setText(fullWord.substring(0, typedCount));
        view.remainingText.setText(fullWord.substring(typedCount));
    }

    private void setupLaser() {
        playerLaser = new Line();
        playerLaser.setStroke(Color.CYAN);
        playerLaser.setStrokeWidth(4);
        playerLaser.setVisible(false);
        gamePane.getChildren().add(playerLaser);
    }

    private void fireLaser(double targetX, double targetY) {
        // Get the translate coordinates to match where the ship actually is
        playerLaser.setStartX(playerShip.getTranslateX() + (playerShip.getFitWidth() / 2));
        playerLaser.setStartY(playerShip.getTranslateY());

        // The target coordinates are also using translate now!
        playerLaser.setEndX(targetX + 20);
        playerLaser.setEndY(targetY + 20);
        playerLaser.setVisible(true);

        laserFramesRemaining = 5;
    }

    private void showStatusFlash(String message) {
        statusText.setText(message);
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), statusText);
        fade.setFromValue(1.0);
        fade.setToValue(0.35);
        fade.play();
    }

    private static class TargetView {
        private final VBox container;
        private final Text typedText;
        private final Text remainingText;

        private TargetView(VBox container, Text typedText, Text remainingText) {
            this.container = container;
            this.typedText = typedText;
            this.remainingText = remainingText;
        }
    }

    private class MainGameLoop extends AnimationTimer {
        @Override
        public void handle(long now) {
            processGameFrame(now);
        }
    }
}