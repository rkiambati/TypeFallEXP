package org.example.controller;

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

import java.util.Random;

public class LoginController {

    @FXML
    private Pane backgroundPane;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorStatus;

    private final AccountManager accountManager = new AccountManager();

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
    private void handleLogin(ActionEvent event) {
        System.out.println("Login button clicked");

        SessionManager.clearSession();

        String username = usernameField == null || usernameField.getText() == null
                ? "" : usernameField.getText().trim();

        String password = passwordField == null || passwordField.getText() == null
                ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and Password are required");
            return;
        }

        Account account = accountManager.authenticateLogin(username, password);

        if (account != null) {
            // Store logged-in user globally for the rest of the app
            SessionManager.setCurrentAccount(account);

            showError("Login Successful");

            // Move to the main page after successful login
            SceneManager.switchScene(
                    event,
                    "/org/example/fxml/main-page-view.fxml",
                    "TypeFall - Main Menu"
            );
        } else {
            showError("Incorrect Username or Password");
            passwordField.clear();
        }
    }

    @FXML
    private void handleInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Help");
        alert.setHeaderText("How to log in");
        alert.setContentText(
                "Enter your username and password to access your account.\n\n" +
                        "If you forgot your password, please contact an administrator."
        );
        alert.showAndWait();
    }

    private void showError(String message) {
        errorStatus.setText(message);
        errorStatus.setVisible(true);
        errorStatus.setManaged(true);
    }
}