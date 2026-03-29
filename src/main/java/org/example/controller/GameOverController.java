package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.SceneManager;

public class GameOverController {

    @FXML
    private void handleRetry(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/example/fxml/gameplay.fxml",
                "TypeFall - Gameplay"
        );
    }

    @FXML
    private void handleMainMenu(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/example/fxml/main-page-view.fxml",
                "TypeFall - Main Menu"
        );
    }
}