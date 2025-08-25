package com.shop.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Simple Welcome page controller without complex animations
 */
public class SimpleWelcomeController implements Initializable {

    @FXML private Button btnGetStarted;
    @FXML private Button btnAbout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Simple initialization without animations
        System.out.println("Welcome Controller initialized successfully!");
    }

    @FXML
    private void handleGetStarted(ActionEvent event) {
        try {
            System.out.println("Loading main application...");
            
            // Load the main application view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent mainView = loader.load();
            
            // Get current stage
            Stage currentStage = (Stage) btnGetStarted.getScene().getWindow();
            
            // Update window title
            currentStage.setTitle("ShopFlow - Management System");
            
            // Create new scene with main view
            Scene mainScene = new Scene(mainView, 1200, 800);
            currentStage.setScene(mainScene);
            
            System.out.println("Main application loaded successfully!");
            
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to load main application: " + ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        try {
            Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
            aboutAlert.setTitle("About ShopFlow");
            aboutAlert.setHeaderText("ShopFlow Management System v1.0");
            aboutAlert.setContentText(
                "ShopFlow is a comprehensive retail management solution.\n\n" +
                "Features:\n" +
                "• Product & Inventory Management\n" +
                "• Customer Relationship Management\n" +
                "• Order Processing & Tracking\n" +
                "• Payment Processing\n" +
                "• Real-time Analytics & Reporting\n\n" +
                "Built with JavaFX and MySQL."
            );
            
            aboutAlert.getDialogPane().setPrefWidth(500);
            aboutAlert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error showing about dialog: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error showing alert: " + e.getMessage());
        }
    }
}
