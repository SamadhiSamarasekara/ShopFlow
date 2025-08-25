package com.shop.controller;

import com.shop.dao.OrderDAO;
import com.shop.dao.CustomerDAO;
import com.shop.model.Order;
import com.shop.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Order management controller with full CRUD operations
 */
public class OrderController implements Initializable {

    @FXML private TableView<Order> tableOrders;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, LocalDateTime> colOrderDate;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, BigDecimal> colTotalAmount;

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private DatePicker dpOrderDate;
    @FXML private ComboBox<Order.OrderStatus> cmbStatus;
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtTaxAmount;
    @FXML private TextField txtDiscountAmount;
    @FXML private TextField txtTotalAmount;
    @FXML private TextArea txtNotes;

    @FXML private Button btnSave;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private OrderDAO orderDAO;
    private CustomerDAO customerDAO;
    private ObservableList<Order> orderList;
    private ObservableList<Customer> customerList;
    private Order selectedOrder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderDAO = new OrderDAO();
        customerDAO = new CustomerDAO();
        orderList = FXCollections.observableArrayList();
        customerList = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupTableSelection();
        setupComboBoxes();
        loadCustomers();
        loadOrders();
        
        // Initially disable edit and delete buttons
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        
        // Add listeners for automatic total calculation
        setupCalculationListeners();
    }

    private void setupTableColumns() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        // Custom cell factory for customer name
        colCustomerName.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            if (order.getCustomer() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
            }
            return new javafx.beans.property.SimpleStringProperty("Unknown Customer");
        });
        
        // Format date column
        colOrderDate.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        
        // Format status column with colors
        colStatus.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "PENDING":
                            setStyle("-fx-text-fill: orange;");
                            break;
                        case "CONFIRMED":
                            setStyle("-fx-text-fill: blue;");
                            break;
                        case "PROCESSING":
                            setStyle("-fx-text-fill: purple;");
                            break;
                        case "SHIPPED":
                            setStyle("-fx-text-fill: teal;");
                            break;
                        case "DELIVERED":
                            setStyle("-fx-text-fill: green;");
                            break;
                        case "CANCELLED":
                        case "REFUNDED":
                            setStyle("-fx-text-fill: red;");
                            break;
                        default:
                            setStyle("-fx-text-fill: black;");
                            break;
                    }
                }
            }
        });
    }

    private void setupTableSelection() {
        tableOrders.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedOrder = newSelection;
                populateFields(newSelection);
                btnEdit.setDisable(false);
                btnDelete.setDisable(false);
            } else {
                selectedOrder = null;
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);
            }
        });
    }

    private void setupComboBoxes() {
        // Setup status combo box
        cmbStatus.setItems(FXCollections.observableArrayList(Order.OrderStatus.values()));
        cmbStatus.setValue(Order.OrderStatus.PENDING);
        
        // Setup customer combo box display
        cmbCustomer.setCellFactory(listView -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (empty || customer == null) {
                    setText(null);
                } else {
                    setText(customer.getFirstName() + " " + customer.getLastName() + " (" + customer.getEmail() + ")");
                }
            }
        });
        
        cmbCustomer.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (empty || customer == null) {
                    setText(null);
                } else {
                    setText(customer.getFirstName() + " " + customer.getLastName());
                }
            }
        });
    }

    private void setupCalculationListeners() {
        txtSubtotal.textProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        txtTaxAmount.textProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        txtDiscountAmount.textProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
    }

    private void calculateTotal() {
        try {
            BigDecimal subtotal = txtSubtotal.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtSubtotal.getText());
            BigDecimal tax = txtTaxAmount.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtTaxAmount.getText());
            BigDecimal discount = txtDiscountAmount.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtDiscountAmount.getText());
            
            BigDecimal total = subtotal.add(tax).subtract(discount);
            txtTotalAmount.setText(total.toString());
        } catch (NumberFormatException e) {
            // Invalid number format, don't update total
        }
    }

    private void populateFields(Order order) {
        // Find and select customer
        for (Customer customer : customerList) {
            if (customer.getCustomerId() == order.getCustomerId()) {
                cmbCustomer.setValue(customer);
                break;
            }
        }
        
        dpOrderDate.setValue(order.getOrderDate().toLocalDate());
        cmbStatus.setValue(order.getStatus());
        txtSubtotal.setText(order.getSubtotal().toString());
        txtTaxAmount.setText(order.getTaxAmount().toString());
        txtDiscountAmount.setText(order.getDiscountAmount().toString());
        txtTotalAmount.setText(order.getTotalAmount().toString());
        txtNotes.setText(order.getNotes());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            Order order = new Order();
            order.setCustomerId(cmbCustomer.getValue().getCustomerId());
            order.setOrderDate(dpOrderDate.getValue().atStartOfDay());
            order.setStatus(cmbStatus.getValue());
            order.setSubtotal(new BigDecimal(txtSubtotal.getText().trim()));
            order.setTaxAmount(new BigDecimal(txtTaxAmount.getText().trim()));
            order.setDiscountAmount(new BigDecimal(txtDiscountAmount.getText().trim()));
            order.setTotalAmount(new BigDecimal(txtTotalAmount.getText().trim()));
            order.setNotes(txtNotes.getText().trim());

            orderDAO.save(order);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order saved successfully!");
            loadOrders();
            clearFields();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving order: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values for amounts.");
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to edit.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            selectedOrder.setCustomerId(cmbCustomer.getValue().getCustomerId());
            selectedOrder.setOrderDate(dpOrderDate.getValue().atStartOfDay());
            selectedOrder.setStatus(cmbStatus.getValue());
            selectedOrder.setSubtotal(new BigDecimal(txtSubtotal.getText().trim()));
            selectedOrder.setTaxAmount(new BigDecimal(txtTaxAmount.getText().trim()));
            selectedOrder.setDiscountAmount(new BigDecimal(txtDiscountAmount.getText().trim()));
            selectedOrder.setTotalAmount(new BigDecimal(txtTotalAmount.getText().trim()));
            selectedOrder.setNotes(txtNotes.getText().trim());

            orderDAO.save(selectedOrder);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order updated successfully!");
            loadOrders();
            clearFields();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating order: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values for amounts.");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Order");
        confirmAlert.setContentText("Are you sure you want to delete order #" + selectedOrder.getOrderId() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                orderDAO.delete(selectedOrder.getOrderId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully!");
                loadOrders();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting order: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        cmbCustomer.setValue(null);
        dpOrderDate.setValue(null);
        cmbStatus.setValue(Order.OrderStatus.PENDING);
        txtSubtotal.clear();
        txtTaxAmount.clear();
        txtDiscountAmount.clear();
        txtTotalAmount.clear();
        txtNotes.clear();
        
        tableOrders.getSelectionModel().clearSelection();
        selectedOrder = null;
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (cmbCustomer.getValue() == null) {
            errors.append("Customer is required.\n");
        }
        
        if (dpOrderDate.getValue() == null) {
            errors.append("Order Date is required.\n");
        }
        
        if (cmbStatus.getValue() == null) {
            errors.append("Status is required.\n");
        }
        
        try {
            if (!txtSubtotal.getText().trim().isEmpty()) {
                new BigDecimal(txtSubtotal.getText().trim());
            }
        } catch (NumberFormatException e) {
            errors.append("Subtotal must be a valid number.\n");
        }
        
        try {
            if (!txtTaxAmount.getText().trim().isEmpty()) {
                new BigDecimal(txtTaxAmount.getText().trim());
            }
        } catch (NumberFormatException e) {
            errors.append("Tax Amount must be a valid number.\n");
        }
        
        try {
            if (!txtDiscountAmount.getText().trim().isEmpty()) {
                new BigDecimal(txtDiscountAmount.getText().trim());
            }
        } catch (NumberFormatException e) {
            errors.append("Discount Amount must be a valid number.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private void loadCustomers() {
        try {
            customerList.clear();
            customerList.addAll(customerDAO.findAll());
            cmbCustomer.setItems(customerList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading customers: " + e.getMessage());
        }
    }

    private void loadOrders() {
        try {
            orderList.clear();
            orderList.addAll(orderDAO.findAll());
            tableOrders.setItems(orderList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading orders: " + e.getMessage());
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
