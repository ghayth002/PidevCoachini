package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            
            // Create the Scene
            Scene scene = new Scene(root);
            
            // Set the Stage
            primaryStage.setTitle("Gestion des Réclamations");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true); // Make the window maximized by default
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Application Error");
            alert.setContentText("Failed to start application: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
