package org.example.controller;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.scene.layout.Pane;
import org.example.SceneManager;

import java.util.Random;

public class HighScoresController {

    @FXML
    private Pane backgroundPane;

    @FXML
    private TableView<HighScoreRow> highScoreTable;

    @FXML
    private TableColumn<HighScoreRow, String> nameColumn;

    @FXML
    private TableColumn<HighScoreRow, Integer> bossesDefeatedColumn;

    @FXML
    private TableColumn<HighScoreRow, String> timePlayedColumn;

    @FXML
    private TableColumn<HighScoreRow, Integer> pointsEarnedColumn;

    private final ObservableList<HighScoreRow> highScoreRows = FXCollections.observableArrayList();

    private final Random random = new Random(); // Added for the asteroids


    @FXML
    public void initialize() {

        // Spawn 15 asteroids when the main menu loads
        for (int i = 0; i < 15; i++) {
            spawnAsteroid();
        }


        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));

        bossesDefeatedColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getBossesDefeated()).asObject());

        timePlayedColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTimePlayed()));

        pointsEarnedColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPointsEarned()).asObject());

        highScoreTable.setItems(highScoreRows);

        loadMockHighScores();
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
    private void handleBackToMainMenu(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/example/fxml/main-page-view.fxml",
                "TypeFall - Main Menu"
        );
    }

    private void loadMockHighScores() {
        highScoreRows.clear();

        highScoreRows.addAll(
                new HighScoreRow("Reich", 12, "18m 42s", 15800),
                new HighScoreRow("Alex", 10, "16m 55s", 14200),
                new HighScoreRow("Brenda", 9, "15m 11s", 13150),
                new HighScoreRow("Charlie", 7, "12m 27s", 10900),
                new HighScoreRow("Noah", 6, "11m 08s", 9800)
        );
    }

    public static class HighScoreRow {
        private final String name;
        private final int bossesDefeated;
        private final String timePlayed;
        private final int pointsEarned;

        public HighScoreRow(String name, int bossesDefeated, String timePlayed, int pointsEarned) {
            this.name = name;
            this.bossesDefeated = bossesDefeated;
            this.timePlayed = timePlayed;
            this.pointsEarned = pointsEarned;
        }

        public String getName() {
            return name;
        }

        public int getBossesDefeated() {
            return bossesDefeated;
        }

        public String getTimePlayed() {
            return timePlayed;
        }

        public int getPointsEarned() {
            return pointsEarned;
        }
    }
}