package com.shop.controller;

import com.shop.dao.CustomerDAO;
import com.shop.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Customer management controller with full CRUD operations
 */
public class CustomerController implements Initializable {

    @FXML private TableView<Customer> tableCustomers;
    @FXML private TableColumn<Customer, Integer> colCustomerId;
    @FXML private TableColumn<Customer, String> colFirstName;
    @FXML private TableColumn<Customer, String> colLastName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhoneNumber;
    @FXML private TableColumn<Customer, String> colCity;
    @FXML private TableColumn<Customer, Boolean> colIsActive;

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhoneNumber;
    @FXML private TextField txtAddress;
    @FXML private TextField txtCity;
    @FXML private TextField txtPostalCode;
    @FXML private TextField txtCountry;
    @FXML private CheckBox chkIsActive;

    @FXML private Button btnSave;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private CustomerDAO customerDAO;
    private ObservableList<Customer> customerList;
    private Customer selectedCustomer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        customerDAO = new CustomerDAO();
        customerList = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupTableSelection();
        loadCustomers();
        
        // Initially disable edit and delete buttons
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void setupTableColumns() {
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colIsActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        // Add boolean cell factory for active status
        colIsActive.setCellFactory(column -> new TableCell<Customer, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Active" : "Inactive");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }

    private void setupTableSelection() {
        tableCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedCustomer = newSelection;
                populateFields(newSelection);
                btnEdit.setDisable(false);
                btnDelete.setDisable(false);
            } else {
                selectedCustomer = null;
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);
            }
        });
    }

    private void populateFields(Customer customer) {
        txtFirstName.setText(customer.getFirstName());
        txtLastName.setText(customer.getLastName());
        txtEmail.setText(customer.getEmail());
        txtPhoneNumber.setText(customer.getPhoneNumber());
        txtAddress.setText(customer.getAddress());
        txtCity.setText(customer.getCity());
        txtPostalCode.setText(customer.getPostalCode());
        txtCountry.setText(customer.getCountry());
        chkIsActive.setSelected(customer.isActive());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            Customer customer = new Customer();
            customer.setFirstName(txtFirstName.getText().trim());
            customer.setLastName(txtLastName.getText().trim());
            customer.setEmail(txtEmail.getText().trim());
            customer.setPhoneNumber(txtPhoneNumber.getText().trim());
            customer.setAddress(txtAddress.getText().trim());
            customer.setCity(txtCity.getText().trim());
            customer.setPostalCode(txtPostalCode.getText().trim());
            customer.setCountry(txtCountry.getText().trim());
            customer.setActive(chkIsActive.isSelected());

            customerDAO.save(customer);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer saved successfully!");
            loadCustomers();
            clearFields();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving customer: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer to edit.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            selectedCustomer.setFirstName(txtFirstName.getText().trim());
            selectedCustomer.setLastName(txtLastName.getText().trim());
            selectedCustomer.setEmail(txtEmail.getText().trim());
            selectedCustomer.setPhoneNumber(txtPhoneNumber.getText().trim());
            selectedCustomer.setAddress(txtAddress.getText().trim());
            selectedCustomer.setCity(txtCity.getText().trim());
            selectedCustomer.setPostalCode(txtPostalCode.getText().trim());
            selectedCustomer.setCountry(txtCountry.getText().trim());
            selectedCustomer.setActive(chkIsActive.isSelected());

            customerDAO.save(selectedCustomer);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
            loadCustomers();
            clearFields();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating customer: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Customer");
        confirmAlert.setContentText("Are you sure you want to delete customer: " + 
                                   selectedCustomer.getFirstName() + " " + selectedCustomer.getLastName() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerDAO.delete(selectedCustomer.getCustomerId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer deleted successfully!");
                loadCustomers();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting customer: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtPhoneNumber.clear();
        txtAddress.clear();
        txtCity.clear();
        txtPostalCode.clear();
        txtCountry.clear();
        chkIsActive.setSelected(true);
        
        tableCustomers.getSelectionModel().clearSelection();
        selectedCustomer = null;
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtFirstName.getText().trim().isEmpty()) {
            errors.append("First Name is required.\n");
        }
        
        if (txtLastName.getText().trim().isEmpty()) {
            errors.append("Last Name is required.\n");
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            errors.append("Email is required.\n");
        } else if (!isValidEmail(txtEmail.getText().trim())) {
            errors.append("Please enter a valid email address.\n");
        }
        
        if (txtPhoneNumber.getText().trim().isEmpty()) {
            errors.append("Phone Number is required.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void loadCustomers() {
        try {
            customerList.clear();
            customerList.addAll(customerDAO.findAll());
            tableCustomers.setItems(customerList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading customers: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
