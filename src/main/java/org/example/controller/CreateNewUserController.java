package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.SceneManager;
import java.util.Random;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;

public class CreateNewUserController {

    @FXML
    private Pane backgroundPane;

    @FXML
    private TextField userName;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField passwordVerification;

    private final Random random = new Random();

    @FXML
    public void initialize() {
        // Spawn 15 asteroids when the login screen loads
        for (int i = 0; i < 15; i++) {
            spawnAsteroid();
        }
    }

    private void spawnAsteroid() {
        // Create the Asteroid using a Circle placeholder
        Circle asteroid = new Circle();

        // Randomize size between 2 and 6 radius
        asteroid.setRadius(random.nextDouble() * 4 + 2);
        asteroid.setFill(Color.web("#8892b0"));
        asteroid.setOpacity(random.nextDouble() * 0.5 + 0.3);

        // Set Starting Position (Off-screen to the left)
        double startY = random.nextDouble() * 720;
        asteroid.setTranslateX(-50);
        asteroid.setTranslateY(startY);

        // Add to the background pane and push to the very back
        backgroundPane.getChildren().add(asteroid);
        asteroid.toBack();

        // Create the Animation
        double durationSeconds = random.nextDouble() * 25 + 15; // 15 to 40 seconds
        TranslateTransition transition = new TranslateTransition(Duration.seconds(durationSeconds), asteroid);

        // Move it across the 1280px screen
        transition.setByX(1350);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(TranslateTransition.INDEFINITE);

        // Start the movement
        transition.play();
    }

    @FXML
    private void handleBackToAccountManagementCU(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/example/fxml/account-management.fxml",
                "TypeFall - Account Management"
        );
    }

    @FXML
    private void handleCancelCU(ActionEvent event) {
        userName.clear();
        password.clear();
        passwordVerification.clear();
    }

    @FXML
    private void handleConfirmCU(ActionEvent event) {
        String username = userName.getText() == null ? "" : userName.getText().trim();
        String pw = password.getText() == null ? "" : password.getText();
        String pwVerify = passwordVerification.getText() == null ? "" : passwordVerification.getText();

        if (username.isEmpty() || pw.isEmpty() || pwVerify.isEmpty()) {
            showInfo("All fields are required.");
            return;
        }

        if (!pw.equals(pwVerify)) {
            showInfo("Passwords do not match.");
            password.clear();
            passwordVerification.clear();
            return;
        }

        // Placeholder until backend user creation is implemented
        showInfo("Validation passed. Backend user creation will be connected next.");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create New User");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}