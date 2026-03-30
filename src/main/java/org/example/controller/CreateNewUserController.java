package org.example.controller;

import java.util.Random;

import org.example.AccountManager;
import org.example.SceneManager;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class CreateNewUserController {

    @FXML
    private Pane backgroundPane;

    @FXML
    private TextField userNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordVerificationField;

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
    private void handleBackToAccountManagementCU(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/example/fxml/account-management.fxml",
                "TypeFall - Account Management"
        );
    }

    @FXML
    private void handleCancelCU(ActionEvent event) {
        userNameField.clear();
        passwordField.clear();
        passwordVerificationField.clear();
    }

    @FXML
    private void handleConfirmCU(ActionEvent event) {
        String userName = userNameField.getText() == null ? "" : userNameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String passwordVerification = passwordVerificationField.getText() == null ? "" : passwordVerificationField.getText();

        if (userName.isEmpty() || password.isEmpty() || passwordVerification.isEmpty()) {
            showInfo("All fields are required.");
            return;
        }

        if (!password.equals(passwordVerification)) {
            showInfo("Passwords do not match.");
            passwordField.clear();
            passwordVerificationField.clear();
            return;
        }

        AccountManager currAccount = new AccountManager();
        currAccount.createAccount(userName, password, false);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create New User");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}