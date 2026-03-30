package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.SceneManager;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;

import java.util.Random;


public class ResetUserPasswordController {

    @FXML
    private Pane backgroundPane;

    @FXML
    private PasswordField oldPassword;

    @FXML
    private PasswordField newPassword;

    @FXML
    private PasswordField confirmNewPassword;

    @FXML
    public void initialize() {
        // Spawn 15 asteroids when the login screen loads
        for (int i = 0; i < 15; i++) {
            spawnAsteroid();
        }
    }

    private final Random random = new Random();


    private void spawnAsteroid() {
        // Create the Asteroid using a Circle placeholder
        Circle asteroid = new Circle();

        // Randomize size between 2 and 6 radius
        asteroid.setRadius(random.nextDouble() * 4 + 2);
        asteroid.setFill(Color.web("#8892b0"));
        asteroid.setOpacity(random.nextDouble() * 0.5 + 0.3);

        // Set Starting Position
        double startY = random.nextDouble() * 720;
        asteroid.setTranslateX(-50);
        asteroid.setTranslateY(startY);

        backgroundPane.getChildren().add(asteroid);
        asteroid.toBack();

        // Create the Animation
        double durationSeconds = random.nextDouble() * 25 + 15; // 15 to 40 seconds
        TranslateTransition transition = new TranslateTransition(Duration.seconds(durationSeconds), asteroid);

        transition.setByX(1350);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(TranslateTransition.INDEFINITE);

        // Start the movement
        transition.play();
    }

    @FXML
    private void handleBackToAccountManagementRP(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/example/fxml/account-management.fxml",
                "TypeFall - Account Management"
        );
    }

    @FXML
    private void handleCancelRP(ActionEvent event) {
        oldPassword.clear();
        newPassword.clear();
        confirmNewPassword.clear();
    }

    @FXML
    private void handleConfirmRP(ActionEvent event) {
        String oldPw = oldPassword.getText() == null ? "" : oldPassword.getText();
        String newPw = newPassword.getText() == null ? "" : newPassword.getText();
        String confirmPw = confirmNewPassword.getText() == null ? "" : confirmNewPassword.getText();

        if (oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            showInfo("All fields are required.");
            return;
        }

        if (!newPw.equals(confirmPw)) {
            showInfo("New passwords do not match.");
            newPassword.clear();
            confirmNewPassword.clear();
            return;
        }

        if (oldPw.equals(newPw)) {
            showInfo("New password must be different from old password.");
            newPassword.clear();
            confirmNewPassword.clear();
            return;
        }

        // Placeholder until backend password reset logic is connected
        showInfo("Validation passed. Backend password reset will be connected next.");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset User Password");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}