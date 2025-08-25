package com.shop.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main controller for the application
 */
public class MainController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox sidebar;

    @FXML
    private Button btnProducts;

    @FXML
    private Button btnCategories;

    @FXML
    private Button btnCustomers;

    @FXML
    private Button btnOrders;

    @FXML
    private Button btnPayments;

    @FXML
    private Button btnReports;

    @FXML
    private Label lblTitle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load the dashboard view by default
        loadDashboard();
    }

    @FXML
    private void handleProductsAction(ActionEvent event) {
        loadView("/fxml/ProductView.fxml", "Products");
    }

    @FXML
    private void handleCategoriesAction(ActionEvent event) {
        loadView("/fxml/CategoryView.fxml", "Categories");
    }

    @FXML
    private void handleCustomersAction(ActionEvent event) {
        loadView("/fxml/CustomerView.fxml", "Customers");
    }

    @FXML
    private void handleOrdersAction(ActionEvent event) {
        loadView("/fxml/OrderView.fxml", "Orders");
    }

    @FXML
    private void handlePaymentsAction(ActionEvent event) {
        loadView("/fxml/PaymentView.fxml", "Payments");
    }

    @FXML
    private void handleReportsAction(ActionEvent event) {
        loadView("/fxml/ReportView.fxml", "Reports");
    }

    @FXML
    private void handleDashboardAction(ActionEvent event) {
        loadDashboard();
    }

    private void loadDashboard() {
        loadView("/fxml/DashboardView.fxml", "Dashboard");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
            lblTitle.setText(title);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
