package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.SceneManager;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import java.util.Random;


public class AccountManagementController {

    @FXML
    private Pane backgroundPane;

    @FXML
    private Button backToPTControlCentre;

    @FXML
    private Button resetUserPasswordPT;

    @FXML
    private Button createNewUserBtnAM;

    @FXML
    private Button selectPlayerAM;

    @FXML
    private TableView<UserRow> userTableAM;

    @FXML
    private TableColumn<UserRow, String> usernameColumnAM;

    @FXML
    private TableColumn<UserRow, String> roleColumnAM;

    private final ObservableList<UserRow> userRows = FXCollections.observableArrayList();

    private UserRow selectedUser;

    private final Random random = new Random();


    @FXML
    public void initialize() {

        // Spawn 15 asteroids when the login screen loads
        for (int i = 0; i < 15; i++) {
            spawnAsteroid();
        }

        resetUserPasswordPT.setDisable(true);
        selectPlayerAM.setDisable(true);

        usernameColumnAM.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername()));
        roleColumnAM.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole()));

        userTableAM.setItems(userRows);

        userTableAM.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectPlayerAM.setDisable(newSelection == null);
        });

        loadMockUsers();
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
    private void handleBackToPTControlCentre(ActionEvent event) {
        System.out.println("Back to clicked");
        SceneManager.switchScene(
                event,
                "/org/example/fxml/parent-teacher-system.fxml",
                "TypeFall - Parent/Teacher Control Centre"
        );
    }

    @FXML
    private void handleCreateNewUser(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/example/fxml/create-new-user.fxml",
                "TypeFall - Create New User"
        );
    }

    @FXML
    private void handleSelectPlayerAM(ActionEvent event) {
        UserRow currentSelection = userTableAM.getSelectionModel().getSelectedItem();

        if (currentSelection == null) {
            showInfo("Please select a user from the table first.");
            return;
        }

        selectedUser = currentSelection;
        resetUserPasswordPT.setDisable(false);
        showInfo("Selected user: " + selectedUser.getUsername());
    }

    @FXML
    private void handleResetUserPasswordPT(ActionEvent event) {
        if (selectedUser == null) {
            showInfo("Please select a user first.");
            return;
        }

        SceneManager.switchScene(
                event,
                "/org/example/fxml/reset-user-password.fxml",
                "TypeFall - Reset User Password"
        );
    }

    private void loadMockUsers() {
        userRows.clear();
        userRows.addAll(
                new UserRow("alex01", "Student"),
                new UserRow("brenda22", "Student"),
                new UserRow("teacherA", "Teacher"),
                new UserRow("parent47", "Parent")
        );
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class UserRow {
        private final String username;
        private final String role;

        public UserRow(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}