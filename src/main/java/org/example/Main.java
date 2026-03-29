package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.controller.GameController;

/**
 * Application entry point.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/gameplay.fxml"));
        Parent root = loader.load();

        GameController controller = loader.getController();
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DIGIT1) {
                controller.usePowerUpSlot(1);
                event.consume();
            } else if (event.getCode() == KeyCode.DIGIT2) {
                controller.usePowerUpSlot(2);
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.ESCAPE) {
                controller.cancelCurrentTarget();
                event.consume();
            }
        });

        scene.setOnKeyTyped(event -> {
            String input = event.getCharacter();

            // Ignore spaces here so they don't count as wrong letters
            if (input == null || input.isEmpty() || " ".equals(input)) {
                return;
            }

            if ("1".equals(input) || "2".equals(input)) {
                return;
            }

            controller.handleKeyPress(input.charAt(0));
        });

        primaryStage.setTitle("TypeFall - Typing Game");
        primaryStage.setFullScreen(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

//package org.example;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.net.URL;
//
//public class Main extends Application {
//
//    @Override
//    public void start(Stage stage) throws IOException {
//        // Use the exact same absolute resource path style
//        // that your controllers already use with SceneManager
//        URL fxmlLocation = getClass().getResource("/org/example/fxml/login-view.fxml");
//
//        // This check helps you catch missing resource paths immediately
//        if (fxmlLocation == null) {
//            throw new RuntimeException("Could not find FXML file at /org/example/fxml/login-view.fxml");
//        }
//
//        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
//        Scene scene = new Scene(fxmlLoader.load());
//
//        stage.setResizable(false);
//
//        stage.setTitle("TypeFall - Login");
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch();
//    }
//}
//
