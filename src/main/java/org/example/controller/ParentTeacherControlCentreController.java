package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.Account;
import org.example.AccountManager;
import org.example.SceneManager;
import java.util.Comparator;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

public class ParentTeacherControlCentreController {
    @FXML
    private Pane backgroundPane; // Added for the asteroids

    @FXML
    private Button backToMainMenuPT;

    @FXML
    private Button accountManagementBtnPT;

    @FXML
    private Button playerStatisticsBtnPT;

    @FXML
    private Button selectPlayerBtnPT;

    @FXML
    private ComboBox<String> sortDropDownBtn;

    @FXML
    private TableView<PlayerRow> playerTablePT;

    @FXML
    private TableColumn<PlayerRow, String> usernameColumnPT;

    @FXML
    private TableColumn<PlayerRow, String> roleColumnPT;

    private final ObservableList<PlayerRow> playerRows = FXCollections.observableArrayList();

    private PlayerRow selectedPlayer;

    // Real backend manager that loads accounts from accounts.json
    private final AccountManager accountManager = new AccountManager();

    private final Random random = new Random(); // Added for the asteroids


    @FXML
    public void initialize() {

        // Spawn 15 asteroids when the main menu loads
        for (int i = 0; i < 15; i++) {
            spawnAsteroid();
        }

        playerStatisticsBtnPT.setDisable(true);
        selectPlayerBtnPT.setDisable(true);

        usernameColumnPT.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername()));
        roleColumnPT.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole()));

        playerTablePT.setItems(playerRows);

        playerTablePT.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectPlayerBtnPT.setDisable(newSelection == null);
        });

        sortDropDownBtn.setItems(FXCollections.observableArrayList(
                "Username A-Z",
                "Username Z-A"
        ));
        sortDropDownBtn.setValue("Username A-Z");

        loadPlayersFromAccounts();
        applySort();
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
    private void handleBackToMainMenuPT(ActionEvent event) {
        SceneManager.switchScene(event,
                "/org/example/fxml/main-page-view.fxml",
                "TypeFall - Main Menu");
    }

    @FXML
    private void handleAccountManagementPT(ActionEvent event) {
        System.out.println("Account Management clicked");
        SceneManager.switchScene(event,
                "/org/example/fxml/account-management.fxml",
                "TypeFall - Account Management");
    }

    @FXML
    private void handleSelectPlayerPT(ActionEvent event) {
        PlayerRow currentSelection = playerTablePT.getSelectionModel().getSelectedItem();

        if (currentSelection == null) {
            showInfo("Please select a player from the table first.");
            return;
        }

        selectedPlayer = currentSelection;
        playerStatisticsBtnPT.setDisable(false);
        showInfo("Selected player: " + selectedPlayer.getUsername());
    }

    @FXML
    private void handlePlayerStatisticsPT(ActionEvent event) {
        if (selectedPlayer == null) {
            showInfo("Please select a player first.");
            return;
        }

        // store selected player somewhere shared if needed.
        SceneManager.switchScene(event,
                "/org/example/fxml/player-statistics.fxml",
                "TypeFall - Player Statistics");
    }

    @FXML
    private void handleSortChangedPT(ActionEvent event) {
        applySort();
    }

    /**
     * Loads real accounts from AccountManager and converts them into table rows.
     *
     * Why we do this conversion:
     * - TableView works nicely with lightweight row objects
     * - Keeps UI display structure separate from full Account objects
     */
    private void loadPlayersFromAccounts() {
        playerRows.clear();

        for (Account account : accountManager.getAllAccounts()) {
            // Convert the boolean admin flag into a display label for now
            String role = account.isAdmin() ? "Admin" : "Player";

            playerRows.add(new PlayerRow(
                    account.getUsername(),
                    role
            ));
        }
    }


    private void applySort() {
        String selectedSort = sortDropDownBtn.getValue();

        if (selectedSort == null) {
            return;
        }

        if (selectedSort.equals("Username A-Z")) {
            FXCollections.sort(playerRows, Comparator.comparing(PlayerRow::getUsername, String.CASE_INSENSITIVE_ORDER));
        } else if (selectedSort.equals("Username Z-A")) {
            FXCollections.sort(playerRows, Comparator.comparing(PlayerRow::getUsername, String.CASE_INSENSITIVE_ORDER).reversed());
        }

        playerTablePT.refresh();
    }

//    private void loadMockPlayers() {
//        playerRows.clear();
//
//        // Temporary mock data so the page works tonight.
//        // Replace this with real account data later.
//        playerRows.addAll(
//                new PlayerRow("alex01", "Student"),
//                new PlayerRow("brenda22", "Student"),
//                new PlayerRow("charlie_dev", "Student"),
//                new PlayerRow("teacherA", "Teacher"),
//                new PlayerRow("parent47", "Parent")
//        );
//    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Parent/Teacher Control Centre");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PlayerRow {
        private final String username;
        private final String role;

        public PlayerRow(String username, String role) {
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