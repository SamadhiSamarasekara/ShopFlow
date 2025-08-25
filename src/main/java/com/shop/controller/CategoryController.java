package com.shop.controller;

import com.shop.dao.CategoryDAO;
import com.shop.model.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Category management controller
 */
public class CategoryController implements Initializable {

    @FXML
    private TableView<Category> tableCategories;

    @FXML
    private TableColumn<Category, Integer> colCategoryId;

    @FXML
    private TableColumn<Category, String> colCategoryName;

    @FXML
    private TableColumn<Category, String> colDescription;

    @FXML
    private TableColumn<Category, LocalDateTime> colCreatedAt;

    @FXML
    private TableColumn<Category, Boolean> colIsActive;

    @FXML
    private TextField txtCategoryName;

    @FXML
    private TextArea txtDescription;

    @FXML
    private CheckBox chkIsActive;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

    private CategoryDAO categoryDAO;
    private ObservableList<Category> categoryList;
    private Category selectedCategory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryDAO = new CategoryDAO();
        categoryList = FXCollections.observableArrayList();

        setupTableColumns();
        setupTableSelection();
        loadCategories();
        
        chkIsActive.setSelected(true); // Default to active
    }

    private void setupTableColumns() {
        colCategoryId.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colIsActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        // Format boolean column
        colIsActive.setCellFactory(column -> new TableCell<Category, Boolean>() {
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
        tableCategories.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedCategory = newValue;
                    populateFields(newValue);
                    updateButtonStates();
                });
    }

    private void populateFields(Category category) {
        if (category != null) {
            txtCategoryName.setText(category.getCategoryName());
            txtDescription.setText(category.getDescription());
            chkIsActive.setSelected(category.isActive());
        } else {
            clearFields();
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedCategory != null;
        btnEdit.setDisable(!hasSelection);
        btnDelete.setDisable(!hasSelection);
    }

    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            Category category;
            if (selectedCategory == null) {
                // Create new category
                category = new Category(txtCategoryName.getText().trim(), txtDescription.getText().trim());
            } else {
                // Update existing category
                category = selectedCategory;
                category.setCategoryName(txtCategoryName.getText().trim());
                category.setDescription(txtDescription.getText().trim());
            }
            
            category.setActive(chkIsActive.isSelected());
            categoryDAO.save(category);
            
            loadCategories();
            clearFields();
            showSuccessMessage("Category saved successfully!");
            
        } catch (SQLException e) {
            showErrorMessage("Error saving category: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditAction(ActionEvent event) {
        if (selectedCategory != null) {
            populateFields(selectedCategory);
            txtCategoryName.requestFocus();
        }
    }

    @FXML
    private void handleDeleteAction(ActionEvent event) {
        if (selectedCategory == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Category");
        alert.setContentText("Are you sure you want to delete this category?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryDAO.softDelete(selectedCategory.getCategoryId());
                loadCategories();
                clearFields();
                showSuccessMessage("Category deleted successfully!");
            } catch (SQLException e) {
                showErrorMessage("Error deleting category: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearAction(ActionEvent event) {
        clearFields();
        tableCategories.getSelectionModel().clearSelection();
    }

    private void loadCategories() {
        try {
            categoryList.clear();
            categoryList.addAll(categoryDAO.findAll());
            tableCategories.setItems(categoryList);
        } catch (SQLException e) {
            showErrorMessage("Error loading categories: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (txtCategoryName.getText().trim().isEmpty()) {
            showErrorMessage("Category name is required!");
            return false;
        }

        try {
            // Check for duplicate names (except current category)
            Optional<Category> existing = categoryDAO.findByName(txtCategoryName.getText().trim());
            if (existing.isPresent() && 
                (selectedCategory == null || existing.get().getCategoryId() != selectedCategory.getCategoryId())) {
                showErrorMessage("Category name already exists!");
                return false;
            }
        } catch (SQLException e) {
            showErrorMessage("Error validating category: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtCategoryName.clear();
        txtDescription.clear();
        chkIsActive.setSelected(true);
        selectedCategory = null;
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
