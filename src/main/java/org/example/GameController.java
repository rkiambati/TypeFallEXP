package org.example;

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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class GameController {

    @FXML private AnchorPane gamePane;
    @FXML private Text scoreText;
    @FXML private Text healthText;
    @FXML private Text levelText;
    @FXML private Text powerupText;
    @FXML private Text statusText;

    private double debugPrintTimer = 0.0;

    private GameSession gameSession;
    private MainGameLoop gameLoop;
    private long lastFrameTime = 0L;

    private final Map<TypingTarget, TargetView> visualElements = new HashMap<>();

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

    @FXML
    public void initialize() {
        enemyImageCache = loadImage("/enemy.png");
        bossImageCache = loadImage("/boss.png");

        setupLaser();

        gameSession = new GameSession(1);
        setupPlayerShip();
        showLevelStartAnimation("Type the enemy words before they hit you");
        gameLoop = new MainGameLoop();
        gameLoop.start();
    }

    private void setupPlayerShip() {
        Image playerImage = loadImage("/player.png");
        if (playerImage != null) {
            playerShip = new ImageView(playerImage);
            playerShip.setFitWidth(100);
            playerShip.setPreserveRatio(true);
        } else {
            playerShip = new ImageView();
            playerShip.setFitWidth(100);
        }

        playerShip.setLayoutX(590);
        playerShip.setLayoutY(540);
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

        if (deltaTime > 0.025) {
            System.out.println(String.format("PULSE STALL deltaTime=%.2fms", deltaTime * 1000.0));
        }

        long t1 = System.nanoTime();
        gameSession.update(deltaTime);
        long t2 = System.nanoTime();

        updateHud();
        long t3 = System.nanoTime();

        syncVisuals();
        long t4 = System.nanoTime();

        debugCounts(deltaTime);

        double updateMs = (t2 - t1) / 1_000_000.0;
        double hudMs = (t3 - t2) / 1_000_000.0;
        double visualsMs = (t4 - t3) / 1_000_000.0;
        double totalMs = (t4 - t1) / 1_000_000.0;

        if (deltaTime > 0.025 || totalMs > 8.0) {
            System.out.println(
                    String.format(
                            "PULSE delta=%.2fms total=%.2fms update=%.2fms hud=%.2fms visuals=%.2fms",
                            deltaTime * 1000.0, totalMs, updateMs, hudMs, visualsMs
                    )
            );
        }

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
            showStatusFlash("Level Cleared");
            gameSession.startLevel(nextLevel);
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

        String currentPowerUpText = "1 Screen Clear x" + gameSession.getScreenClearCharges() +
                "   2 Heal (+10) x" + gameSession.getHealCharges();
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
        Set<TypingTarget> fastLookupSet = new HashSet<>(gameSession.getActiveTargets());

        for (TypingTarget target : gameSession.getActiveTargets()) {
            TargetView view = visualElements.computeIfAbsent(target, this::createTargetView);
            view.container.setLayoutX(target.getX());
            view.container.setLayoutY(target.getY());

            if (target.getTypedCharacterIndex() > 0) {
                if (target == activeTarget && target.getTypedCharacterIndex() > lastTypedIndex) {
                    updateTargetText(view, target);
                    fireLaser(target.getX(), target.getY());
                    lastTypedIndex = target.getTypedCharacterIndex();
                } else if (target != activeTarget) {
                    activeTarget = target;
                    lastTypedIndex = target.getTypedCharacterIndex();
                    updateTargetText(view, target);
                    fireLaser(target.getX(), target.getY());
                }
            }
        }

        visualElements.entrySet().removeIf(entry -> {
            TypingTarget target = entry.getKey();
            TargetView view = entry.getValue();

            if (!fastLookupSet.contains(target)) {
                gamePane.getChildren().remove(view.container);
                if (target == activeTarget) {
                    activeTarget = null;
                    lastTypedIndex = 0;
                }
                return true;
            }
            return false;
        });
    }

    private TargetView createTargetView(TypingTarget target) {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(4);

        Node headerNode = createVisualNodeForTarget(target);

        Text typedText = new Text();
        Text remainingText = new Text();
        TextFlow textFlow = new TextFlow(typedText, remainingText);
        textFlow.setTextAlignment(TextAlignment.CENTER);

        styleTextForTarget(target, typedText, remainingText);

        container.getChildren().addAll(headerNode, textFlow);
        gamePane.getChildren().add(container);

        TargetView view = new TargetView(container, typedText, remainingText);
        updateTargetText(view, target);
        return view;
    }

    private Node createVisualNodeForTarget(TypingTarget target) {
        if (target instanceof BossEnemy) {
            if (bossImageCache != null) {
                ImageView imageView = new ImageView(bossImageCache);
                imageView.setFitWidth(150);
                imageView.setPreserveRatio(true);
                return imageView;
            }
            Text fallback = new Text("BOSS");
            fallback.setFill(Color.ORANGERED);
            fallback.setFont(Font.font("System Bold", 28));
            return fallback;
        }

        if (target instanceof PowerUpDrop powerUpDrop) {
            boolean isClear = powerUpDrop.getPowerUpType() == PowerUpType.SCREEN_CLEAR;
            Text badge = new Text(isClear ? "CLEAR" : "HEAL");
            badge.setFill(isClear ? Color.GOLD : Color.HOTPINK);
            badge.setFont(Font.font("System Bold", 20));
            return badge;
        }

        if (enemyImageCache != null) {
            ImageView imageView = new ImageView(enemyImageCache);
            imageView.setFitWidth(80);
            imageView.setPreserveRatio(true);
            return imageView;
        }

        Text fallback = new Text("ENEMY");
        fallback.setFill(Color.WHITE);
        fallback.setFont(Font.font("System Bold", 20));
        return fallback;
    }

    private void styleTextForTarget(TypingTarget target, Text typedText, Text remainingText) {
        if (target instanceof BossEnemy) {
            typedText.setStyle("-fx-font-size: 34px; -fx-font-weight: bold;");
            remainingText.setStyle("-fx-font-size: 34px; -fx-font-weight: bold;");
            typedText.setFill(Color.LIME);
            remainingText.setFill(Color.web("#ff4d4d"));
            return;
        }

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
        playerLaser.setStartX(playerShip.getLayoutX() + (playerShip.getFitWidth() / 2));
        playerLaser.setStartY(playerShip.getLayoutY());
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

    private void debugCounts(double deltaTime) {
        debugPrintTimer += deltaTime;

        if (debugPrintTimer >= 0.5) {
            debugPrintTimer = 0.0;

            System.out.println(
                    "logic=" + gameSession.getActiveTargets().size()
                            + " visuals=" + visualElements.size()
                            + " children=" + gamePane.getChildren().size()
            );
        }
    }

    private class MainGameLoop extends AnimationTimer {
        @Override
        public void handle(long now) {
            processGameFrame(now);
        }
    }
}