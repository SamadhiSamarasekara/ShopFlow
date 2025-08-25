package com.shop.controller;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Welcome page controller with smooth transitions and animations
 */
public class WelcomeController implements Initializable {

    @FXML private Button btnGetStarted;
    @FXML private Button btnAbout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add subtle animations to buttons
        setupButtonAnimations();
        
        // Add entrance animations
        setupEntranceAnimations();
    }

    private void setupButtonAnimations() {
        // Get Started button hover effect
        btnGetStarted.setOnMouseEntered(e -> {
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), btnGetStarted);
            scaleIn.setToX(1.05);
            scaleIn.setToY(1.05);
            
            // Add rotation effect
            RotateTransition rotate = new RotateTransition(Duration.millis(200), btnGetStarted);
            rotate.setToAngle(2);
            
            ParallelTransition parallel = new ParallelTransition(scaleIn, rotate);
            parallel.play();
        });

        btnGetStarted.setOnMouseExited(e -> {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), btnGetStarted);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            
            RotateTransition rotate = new RotateTransition(Duration.millis(200), btnGetStarted);
            rotate.setToAngle(0);
            
            ParallelTransition parallel = new ParallelTransition(scaleOut, rotate);
            parallel.play();
        });

        // Learn More button hover effect
        btnAbout.setOnMouseEntered(e -> {
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), btnAbout);
            scaleIn.setToX(1.05);
            scaleIn.setToY(1.05);
            scaleIn.play();
        });

        btnAbout.setOnMouseExited(e -> {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), btnAbout);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
        });
    }

    private void setupEntranceAnimations() {
        // Get the main container
        Parent root = btnGetStarted.getScene().getRoot();
        
        // Create a timeline for staggered animations
        Timeline entranceTimeline = new Timeline();
        
        // Animate child elements with staggered timing
        animateChildrenWithDelay(root, entranceTimeline, 0);
        
        entranceTimeline.play();
    }

    private void animateChildrenWithDelay(Parent parent, Timeline timeline, double delay) {
        if (parent instanceof VBox) {
            VBox vbox = (VBox) parent;
            for (int i = 0; i < vbox.getChildren().size(); i++) {
                Node child = vbox.getChildren().get(i);
                
                // Create fade in and slide up animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(800), child);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                
                TranslateTransition slideUp = new TranslateTransition(Duration.millis(800), child);
                slideUp.setFromY(30);
                slideUp.setToY(0);
                
                // Set initial state
                child.setOpacity(0.0);
                child.setTranslateY(30);
                
                // Add to timeline with delay
                KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(delay + (i * 150)),
                    e -> {
                        ParallelTransition parallel = new ParallelTransition(fadeIn, slideUp);
                        parallel.play();
                    }
                );
                timeline.getKeyFrames().add(keyFrame);
            }
        }
    }

    @FXML
    private void handleGetStarted(ActionEvent event) {
        try {
            // Create pulsing effect before transition
            ScaleTransition pulse = new ScaleTransition(Duration.millis(150), btnGetStarted);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            
            pulse.setOnFinished(e -> {
                // Create fade out transition for smooth transition
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), btnGetStarted.getScene().getRoot());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                
                fadeOut.setOnFinished(finishEvent -> {
                    try {
                        // Load the main application view
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                        Parent mainView = loader.load();
                        
                        // Get current stage
                        Stage currentStage = (Stage) btnGetStarted.getScene().getWindow();
                        
                        // Update window title
                        currentStage.setTitle("ShopFlow - Management System");
                        
                        // Create new scene with main view
                        Scene mainScene = new Scene(mainView, 1200, 800);
                        
                        // Set the new scene with fade in effect
                        currentStage.setScene(mainScene);
                        
                        // Apply fade in transition
                        mainView.setOpacity(0.0);
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), mainView);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                        
                    } catch (IOException ex) {
                        showAlert("Error", "Failed to load main application: " + ex.getMessage());
                    }
                });
                
                fadeOut.play();
            });
            
            pulse.play();
            
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        // Add button click animation
        ScaleTransition clickAnimation = new ScaleTransition(Duration.millis(100), btnAbout);
        clickAnimation.setToX(0.95);
        clickAnimation.setToY(0.95);
        clickAnimation.setAutoReverse(true);
        clickAnimation.setCycleCount(2);
        
        clickAnimation.setOnFinished(e -> showAboutDialog());
        clickAnimation.play();
    }

    private void showAboutDialog() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About ShopFlow");
        aboutAlert.setHeaderText("🏪 ShopFlow Management System v1.0");
        aboutAlert.setContentText(
            "ShopFlow is a comprehensive retail management solution designed to streamline your business operations.\n\n" +
            "✨ Key Features:\n" +
            "• 📦 Product & Inventory Management\n" +
            "• 👥 Customer Relationship Management\n" +
            "• 📋 Order Processing & Tracking\n" +
            "• 💳 Payment Processing\n" +
            "• 📊 Real-time Analytics & Reporting\n" +
            "• 👤 Multi-user Support\n" +
            "• 🔒 Secure Data Management\n\n" +
            "🛠️ Technical Stack:\n" +
            "Built with JavaFX and MySQL for optimal performance and reliability.\n" +
            "Perfect for small to medium-sized retail businesses.\n\n" +
            "🚀 Get started in minutes with our intuitive interface!"
        );
        
        aboutAlert.getDialogPane().setPrefWidth(550);
        aboutAlert.getDialogPane().setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);" +
            "-fx-text-fill: white;"
        );
        aboutAlert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("⚠️ " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
