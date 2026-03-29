package org.example.controller;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.Account;
import org.example.SceneManager;
import org.example.SessionManager;

import java.util.Random;

public class MainPageController {

    @FXML
    private Pane backgroundPane; // Added for the asteroids

    @FXML
    private Button parentalControlButton;

    private final Random random = new Random(); // Added for the asteroids


    @FXML
    public void initialize() {
        // Spawn 15 asteroids when the main menu loads
        for (int i = 0; i < 15; i++) {
            spawnAsteroid();
        }
    }

    private void spawnAsteroid() {
        Circle asteroid = new Circle();
        asteroid.setRadius(random.nextDouble() * 4 + 2);
        asteroid.setFill(Color.web("#8892b0"));
        asteroid.setOpacity(random.nextDouble() * 0.5 + 0.3);

        double startY = random.nextDouble() * 720;
        asteroid.setTranslateX(-50);
        asteroid.setTranslateY(startY);

        backgroundPane.getChildren().add(asteroid);
        asteroid.toBack();

        double durationSeconds = random.nextDouble() * 25 + 15;
        TranslateTransition transition = new TranslateTransition(Duration.seconds(durationSeconds), asteroid);

        transition.setByX(1350);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(TranslateTransition.INDEFINITE);

        transition.play();
    }
    @FXML
    private void handlePlayGame(ActionEvent event) {
        SceneManager.switchScene(event, "/org/example/fxml/gameplay.fxml", "TypeFall - Gameplay");
    }

    @FXML
    private void handleHighScores(ActionEvent event) {
        SceneManager.switchScene(event, "/org/example/fxml/high-scores.fxml", "TypeFall - High Scores");
    }

    @FXML
    private void handleTutorial(ActionEvent event) {
        SceneManager.switchScene(event, "/org/example/fxml/tutorial.fxml", "TypeFall - Tutorial");
    }

    @FXML
    private void handleParentalControls(ActionEvent event) {
        // Read the currently logged-in account from session
        Account currentAccount = SessionManager.getCurrentAccount();

        // Basic safety check
        if (currentAccount == null) {
            showInfo("No active session found. Please log in again.");
            SceneManager.switchScene(event, "/org/example/fxml/login-view.fxml", "TypeFall - Login");
            return;
        }

        // Only allow admin accounts into parent/teacher controls for now
        if (!currentAccount.isAdmin()) {
            showInfo("Access denied. Only the admin account can access parental controls right now.");
            return;
        }

        // If admin, allow access
        SceneManager.switchScene(
                event,
                "/org/example/fxml/parent-teacher-system.fxml",
                "TypeFall - Parent/Teacher Control Centre"
        );
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        SceneManager.switchScene(event, "/org/example/fxml/login-view.fxml", "TypeFall - Login");
    }

    /**
     * Small helper to show simple informational popups.
     *
     * Why this exists:
     * - Keeps alert code out of the button handlers
     * - Makes the controller easier to read
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TypeFall");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}