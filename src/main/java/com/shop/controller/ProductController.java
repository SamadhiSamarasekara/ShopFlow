package com.shop.controller;

import com.shop.dao.ProductDAO;
import com.shop.dao.CategoryDAO;
import com.shop.model.Product;
import com.shop.model.Category;
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
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Product management controller with full CRUD operations
 */
public class ProductController implements Initializable {

    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colProductId;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colSku;
    @FXML private TableColumn<Product, BigDecimal> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Boolean> colIsActive;

    @FXML private TextField txtProductName;
    @FXML private TextField txtSku;
    @FXML private TextField txtPrice;
    @FXML private TextField txtCostPrice;
    @FXML private TextField txtStockQuantity;
    @FXML private TextField txtMinStockLevel;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<Category> cmbCategory;
    @FXML private CheckBox chkIsActive;

    @FXML private Button btnSave;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private ObservableList<Product> productList;
    private Product selectedProduct;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
        productList = FXCollections.observableArrayList();

        setupTableColumns();
        setupTableSelection();
        loadCategories();
        loadProducts();
        
        chkIsActive.setSelected(true);
    }

    private void setupTableColumns() {
        colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Category column
        colCategory.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return new javafx.beans.property.SimpleStringProperty(
                category != null ? category.getCategoryName() : "No Category"
            );
        });

        // Status column
        colIsActive.setCellFactory(column -> new TableCell<Product, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item ? "Active" : "Inactive");
                }
            }
        });
    }

    private void setupTableSelection() {
        tableProducts.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedProduct = newValue;
                    populateFields(newValue);
                    updateButtonStates();
                });
    }

    private void populateFields(Product product) {
        if (product != null) {
            txtProductName.setText(product.getProductName());
            txtSku.setText(product.getSku());
            txtPrice.setText(product.getPrice().toString());
            txtCostPrice.setText(product.getCostPrice() != null ? product.getCostPrice().toString() : "");
            txtStockQuantity.setText(String.valueOf(product.getStockQuantity()));
            txtMinStockLevel.setText(String.valueOf(product.getMinStockLevel()));
            txtDescription.setText(product.getDescription());
            cmbCategory.setValue(product.getCategory());
            chkIsActive.setSelected(product.isActive());
        } else {
            clearFields();
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedProduct != null;
        btnEdit.setDisable(!hasSelection);
        btnDelete.setDisable(!hasSelection);
    }

    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            Product product;
            if (selectedProduct == null) {
                // Create new product
                product = new Product();
            } else {
                // Update existing product
                product = selectedProduct;
            }
            
            // Set product data
            product.setProductName(txtProductName.getText().trim());
            product.setSku(txtSku.getText().trim());
            product.setPrice(new BigDecimal(txtPrice.getText().trim()));
            
            if (!txtCostPrice.getText().trim().isEmpty()) {
                product.setCostPrice(new BigDecimal(txtCostPrice.getText().trim()));
            }
            
            product.setStockQuantity(Integer.parseInt(txtStockQuantity.getText().trim()));
            product.setMinStockLevel(Integer.parseInt(txtMinStockLevel.getText().trim()));
            product.setDescription(txtDescription.getText().trim());
            product.setActive(chkIsActive.isSelected());
            
            if (cmbCategory.getValue() != null) {
                product.setCategory(cmbCategory.getValue());
            }
            
            productDAO.save(product);
            
            loadProducts();
            clearFields();
            showSuccessMessage("Product saved successfully!");
            
        } catch (SQLException e) {
            showErrorMessage("Error saving product: " + e.getMessage());
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter valid numbers for price and stock fields.");
        }
    }

    @FXML
    private void handleEditAction(ActionEvent event) {
        if (selectedProduct != null) {
            populateFields(selectedProduct);
            txtProductName.requestFocus();
        }
    }

    @FXML
    private void handleDeleteAction(ActionEvent event) {
        if (selectedProduct == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to delete this product?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productDAO.softDelete(selectedProduct.getProductId());
                loadProducts();
                clearFields();
                showSuccessMessage("Product deleted successfully!");
            } catch (SQLException e) {
                showErrorMessage("Error deleting product: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearAction(ActionEvent event) {
        clearFields();
        tableProducts.getSelectionModel().clearSelection();
    }

    private void loadCategories() {
        try {
            ObservableList<Category> categories = FXCollections.observableArrayList();
            categories.addAll(categoryDAO.findActive());
            cmbCategory.setItems(categories);
        } catch (SQLException e) {
            showErrorMessage("Error loading categories: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            productList.clear();
            productList.addAll(productDAO.findAll());
            tableProducts.setItems(productList);
        } catch (SQLException e) {
            showErrorMessage("Error loading products: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Product name validation
        if (txtProductName.getText().trim().isEmpty()) {
            showErrorMessage("Product name is required!");
            return false;
        }

        // SKU validation
        if (txtSku.getText().trim().isEmpty()) {
            showErrorMessage("SKU is required!");
            return false;
        }

        // Price validation
        try {
            BigDecimal price = new BigDecimal(txtPrice.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                showErrorMessage("Price cannot be negative!");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter a valid price!");
            return false;
        }

        // Cost price validation (optional)
        if (!txtCostPrice.getText().trim().isEmpty()) {
            try {
                BigDecimal costPrice = new BigDecimal(txtCostPrice.getText().trim());
                if (costPrice.compareTo(BigDecimal.ZERO) < 0) {
                    showErrorMessage("Cost price cannot be negative!");
                    return false;
                }
            } catch (NumberFormatException e) {
                showErrorMessage("Please enter a valid cost price!");
                return false;
            }
        }

        // Stock quantity validation
        try {
            int stock = Integer.parseInt(txtStockQuantity.getText().trim());
            if (stock < 0) {
                showErrorMessage("Stock quantity cannot be negative!");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter a valid stock quantity!");
            return false;
        }

        // Min stock level validation
        try {
            int minStock = Integer.parseInt(txtMinStockLevel.getText().trim());
            if (minStock < 0) {
                showErrorMessage("Minimum stock level cannot be negative!");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter a valid minimum stock level!");
            return false;
        }

        // Check for duplicate SKU
        try {
            Optional<Product> existing = productDAO.findBySku(txtSku.getText().trim());
            if (existing.isPresent() && 
                (selectedProduct == null || existing.get().getProductId() != selectedProduct.getProductId())) {
                showErrorMessage("SKU already exists!");
                return false;
            }
        } catch (SQLException e) {
            showErrorMessage("Error validating product: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtProductName.clear();
        txtSku.clear();
        txtPrice.clear();
        txtCostPrice.clear();
        txtStockQuantity.clear();
        txtMinStockLevel.clear();
        txtDescription.clear();
        cmbCategory.setValue(null);
        chkIsActive.setSelected(true);
        selectedProduct = null;
        updateButtonStates();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
