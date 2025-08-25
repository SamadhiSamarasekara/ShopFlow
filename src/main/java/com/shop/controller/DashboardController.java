package com.shop.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Dashboard controller
 */
public class DashboardController implements Initializable {

    @FXML
    private Label lblTotalProducts;

    @FXML
    private Label lblTotalCustomers;

    @FXML
    private Label lblTotalOrders;

    @FXML
    private Label lblTotalRevenue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboardData();
    }

    private void loadDashboardData() {
        // TODO: Load actual data from database
        lblTotalProducts.setText("0");
        lblTotalCustomers.setText("0");
        lblTotalOrders.setText("0");
        lblTotalRevenue.setText("$0.00");
    }
}
