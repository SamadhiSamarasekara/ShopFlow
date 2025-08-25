package com.shop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            System.out.println("Starting ShopFlow Application...");
            
            // Try to load the welcome page first
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SimpleWelcomeView.fxml"));
            
            primaryStage.setTitle("ShopFlow - Welcome");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            System.out.println("Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("Error loading welcome page: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Load main application directly
            try {
                System.out.println("Loading main application as fallback...");
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
                
                primaryStage.setTitle("ShopFlow - Management System");
                primaryStage.setScene(new Scene(root, 1200, 800));
                primaryStage.setMinWidth(800);
                primaryStage.setMinHeight(600);
                primaryStage.setResizable(true);
                primaryStage.centerOnScreen();
                primaryStage.show();
                
                System.out.println("Fallback loaded successfully!");
            } catch (Exception fallbackError) {
                System.err.println("Error loading fallback: " + fallbackError.getMessage());
                fallbackError.printStackTrace();
                throw fallbackError;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
