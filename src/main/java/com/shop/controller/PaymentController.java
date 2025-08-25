package com.shop.controller;

import com.shop.dao.PaymentDAO;
import com.shop.dao.OrderDAO;
import com.shop.model.Payment;
import com.shop.model.Order;
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
 * Payment management controller with full CRUD operations
 */
public class PaymentController implements Initializable {

    @FXML private TableView<Payment> tablePayments;
    @FXML private TableColumn<Payment, Integer> colPaymentId;
    @FXML private TableColumn<Payment, Integer> colOrderId;
    @FXML private TableColumn<Payment, String> colPaymentMethod;
    @FXML private TableColumn<Payment, String> colStatus;
    @FXML private TableColumn<Payment, BigDecimal> colAmount;
    @FXML private TableColumn<Payment, LocalDateTime> colPaymentDate;

    @FXML private ComboBox<Order> cmbOrder;
    @FXML private ComboBox<Payment.PaymentMethod> cmbPaymentMethod;
    @FXML private ComboBox<Payment.PaymentStatus> cmbStatus;
    @FXML private TextField txtAmount;
    @FXML private TextField txtTransactionId;
    @FXML private TextField txtReference;
    @FXML private DatePicker dpPaymentDate;
    @FXML private TextArea txtNotes;

    @FXML private Button btnSave;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private PaymentDAO paymentDAO;
    private OrderDAO orderDAO;
    private ObservableList<Payment> paymentList;
    private ObservableList<Order> orderList;
    private Payment selectedPayment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paymentDAO = new PaymentDAO();
        orderDAO = new OrderDAO();
        paymentList = FXCollections.observableArrayList();
        orderList = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupTableSelection();
        setupComboBoxes();
        loadOrders();
        loadPayments();
        
        // Initially disable edit and delete buttons
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void setupTableColumns() {
        colPaymentId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colPaymentDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        
        // Format date column
        colPaymentDate.setCellFactory(column -> new TableCell<Payment, LocalDateTime>() {
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
        colStatus.setCellFactory(column -> new TableCell<Payment, String>() {
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
                        case "PROCESSING":
                            setStyle("-fx-text-fill: blue;");
                            break;
                        case "COMPLETED":
                            setStyle("-fx-text-fill: green;");
                            break;
                        case "FAILED":
                        case "CANCELLED":
                            setStyle("-fx-text-fill: red;");
                            break;
                        case "REFUNDED":
                        case "PARTIAL_REFUND":
                            setStyle("-fx-text-fill: purple;");
                            break;
                        default:
                            setStyle("-fx-text-fill: black;");
                            break;
                    }
                }
            }
        });
        
        // Format payment method column
        colPaymentMethod.setCellFactory(column -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.replace("_", " "));
                }
            }
        });
    }

    private void setupTableSelection() {
        tablePayments.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPayment = newSelection;
                populateFields(newSelection);
                btnEdit.setDisable(false);
                btnDelete.setDisable(false);
            } else {
                selectedPayment = null;
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);
            }
        });
    }

    private void setupComboBoxes() {
        // Setup payment method combo box
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(Payment.PaymentMethod.values()));
        cmbPaymentMethod.setValue(Payment.PaymentMethod.CASH);
        
        // Setup status combo box
        cmbStatus.setItems(FXCollections.observableArrayList(Payment.PaymentStatus.values()));
        cmbStatus.setValue(Payment.PaymentStatus.PENDING);
        
        // Setup order combo box display
        cmbOrder.setCellFactory(listView -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                } else {
                    String customerName = order.getCustomer() != null ? 
                        order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() : 
                        "Unknown Customer";
                    setText("Order #" + order.getOrderId() + " - " + customerName + " ($" + order.getTotalAmount() + ")");
                }
            }
        });
        
        cmbOrder.setButtonCell(new ListCell<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                } else {
                    setText("Order #" + order.getOrderId());
                }
            }
        });
    }

    private void populateFields(Payment payment) {
        // Find and select order
        for (Order order : orderList) {
            if (order.getOrderId() == payment.getOrderId()) {
                cmbOrder.setValue(order);
                break;
            }
        }
        
        cmbPaymentMethod.setValue(payment.getPaymentMethod());
        cmbStatus.setValue(payment.getStatus());
        txtAmount.setText(payment.getAmount().toString());
        txtTransactionId.setText(payment.getTransactionId());
        txtReference.setText(payment.getReference());
        dpPaymentDate.setValue(payment.getPaymentDate().toLocalDate());
        txtNotes.setText(payment.getNotes());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            Payment payment = new Payment();
            payment.setOrderId(cmbOrder.getValue().getOrderId());
            payment.setPaymentMethod(cmbPaymentMethod.getValue());
            payment.setStatus(cmbStatus.getValue());
            payment.setAmount(new BigDecimal(txtAmount.getText().trim()));
            payment.setTransactionId(txtTransactionId.getText().trim());
            payment.setReference(txtReference.getText().trim());
            payment.setPaymentDate(dpPaymentDate.getValue().atStartOfDay());
            payment.setNotes(txtNotes.getText().trim());

            paymentDAO.save(payment);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Payment saved successfully!");
            loadPayments();
            clearFields();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving payment: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid amount.");
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (selectedPayment == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a payment to edit.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            selectedPayment.setOrderId(cmbOrder.getValue().getOrderId());
            selectedPayment.setPaymentMethod(cmbPaymentMethod.getValue());
            selectedPayment.setStatus(cmbStatus.getValue());
            selectedPayment.setAmount(new BigDecimal(txtAmount.getText().trim()));
            selectedPayment.setTransactionId(txtTransactionId.getText().trim());
            selectedPayment.setReference(txtReference.getText().trim());
            selectedPayment.setPaymentDate(dpPaymentDate.getValue().atStartOfDay());
            selectedPayment.setNotes(txtNotes.getText().trim());

            paymentDAO.save(selectedPayment);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Payment updated successfully!");
            loadPayments();
            clearFields();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating payment: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid amount.");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedPayment == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a payment to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Payment");
        confirmAlert.setContentText("Are you sure you want to delete payment #" + selectedPayment.getPaymentId() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                paymentDAO.delete(selectedPayment.getPaymentId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Payment deleted successfully!");
                loadPayments();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting payment: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        cmbOrder.setValue(null);
        cmbPaymentMethod.setValue(Payment.PaymentMethod.CASH);
        cmbStatus.setValue(Payment.PaymentStatus.PENDING);
        txtAmount.clear();
        txtTransactionId.clear();
        txtReference.clear();
        dpPaymentDate.setValue(null);
        txtNotes.clear();
        
        tablePayments.getSelectionModel().clearSelection();
        selectedPayment = null;
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (cmbOrder.getValue() == null) {
            errors.append("Order is required.\n");
        }
        
        if (cmbPaymentMethod.getValue() == null) {
            errors.append("Payment Method is required.\n");
        }
        
        if (cmbStatus.getValue() == null) {
            errors.append("Status is required.\n");
        }
        
        if (txtAmount.getText().trim().isEmpty()) {
            errors.append("Amount is required.\n");
        } else {
            try {
                BigDecimal amount = new BigDecimal(txtAmount.getText().trim());
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    errors.append("Amount must be positive.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Amount must be a valid number.\n");
            }
        }
        
        if (dpPaymentDate.getValue() == null) {
            errors.append("Payment Date is required.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private void loadOrders() {
        try {
            orderList.clear();
            orderList.addAll(orderDAO.findAll());
            cmbOrder.setItems(orderList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading orders: " + e.getMessage());
        }
    }

    private void loadPayments() {
        try {
            paymentList.clear();
            paymentList.addAll(paymentDAO.findAll());
            tablePayments.setItems(paymentList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading payments: " + e.getMessage());
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
