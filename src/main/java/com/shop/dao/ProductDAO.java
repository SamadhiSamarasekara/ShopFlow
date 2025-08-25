package com.shop.dao;

import com.shop.database.DatabaseConfig;
import com.shop.model.Product;
import com.shop.model.Category;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Product operations
 */
public class ProductDAO {
    private final DatabaseConfig dbConfig;

    public ProductDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public Product save(Product product) throws SQLException {
        if (product.getProductId() == 0) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    private Product insert(Product product) throws SQLException {
        String sql = """
            INSERT INTO products (product_name, description, sku, price, cost_price, 
                                stock_quantity, min_stock_level, category_id, image_url, 
                                created_at, updated_at, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setString(3, product.getSku());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setBigDecimal(5, product.getCostPrice());
            stmt.setInt(6, product.getStockQuantity());
            stmt.setInt(7, product.getMinStockLevel());
            stmt.setInt(8, product.getCategoryId());
            stmt.setString(9, product.getImageUrl());
            stmt.setTimestamp(10, Timestamp.valueOf(product.getCreatedAt()));
            stmt.setTimestamp(11, Timestamp.valueOf(product.getUpdatedAt()));
            stmt.setBoolean(12, product.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setProductId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
        }
        return product;
    }

    private Product update(Product product) throws SQLException {
        String sql = """
            UPDATE products SET product_name = ?, description = ?, sku = ?, price = ?, 
                              cost_price = ?, stock_quantity = ?, min_stock_level = ?, 
                              category_id = ?, image_url = ?, updated_at = ?, is_active = ?
            WHERE product_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setString(3, product.getSku());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setBigDecimal(5, product.getCostPrice());
            stmt.setInt(6, product.getStockQuantity());
            stmt.setInt(7, product.getMinStockLevel());
            stmt.setInt(8, product.getCategoryId());
            stmt.setString(9, product.getImageUrl());
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(11, product.isActive());
            stmt.setInt(12, product.getProductId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating product failed, no rows affected.");
            }
        }
        return product;
    }

    public Optional<Product> findById(int productId) throws SQLException {
        String sql = """
            SELECT p.*, c.category_name, c.description as category_description
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.category_id 
            WHERE p.product_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        }
        return Optional.empty();
    }

    public List<Product> findAll() throws SQLException {
        String sql = """
            SELECT p.*, c.category_name, c.description as category_description
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.category_id 
            ORDER BY p.product_name
        """;
        
        return executeProductQuery(sql);
    }

    public List<Product> findActive() throws SQLException {
        String sql = """
            SELECT p.*, c.category_name, c.description as category_description
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.category_id 
            WHERE p.is_active = TRUE 
            ORDER BY p.product_name
        """;
        
        return executeProductQuery(sql);
    }

    public List<Product> findByCategory(int categoryId) throws SQLException {
        String sql = """
            SELECT p.*, c.category_name, c.description as category_description
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.category_id 
            WHERE p.category_id = ? AND p.is_active = TRUE 
            ORDER BY p.product_name
        """;

        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> findLowStock() throws SQLException {
        String sql = """
            SELECT p.*, c.category_name, c.description as category_description
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.category_id 
            WHERE p.stock_quantity <= p.min_stock_level AND p.is_active = TRUE 
            ORDER BY p.stock_quantity ASC
        """;
        
        return executeProductQuery(sql);
    }

    public Optional<Product> findBySku(String sku) throws SQLException {
        String sql = """
            SELECT p.*, c.category_name, c.description as category_description
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.category_id 
            WHERE p.sku = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, sku);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        }
        return Optional.empty();
    }

    public List<Product> searchByName(String searchTerm) throws SQLException {
        String sql = """
            SELECT p.*, c.category_name, c.description as category_description
            FROM products p 
            LEFT JOIN categories c ON p.category_id = c.category_id 
            WHERE p.product_name ILIKE ? AND p.is_active = TRUE 
            ORDER BY p.product_name
        """;

        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public boolean updateStock(int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ?, updated_at = ? WHERE product_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean softDelete(int productId) throws SQLException {
        String sql = "UPDATE products SET is_active = FALSE, updated_at = ? WHERE product_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existsBySku(String sku) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE sku = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, sku);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private List<Product> executeProductQuery(String sql) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getString("sku"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("cost_price"),
                rs.getInt("stock_quantity"),
                rs.getInt("min_stock_level"),
                rs.getInt("category_id"),
                rs.getString("image_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime(),
                rs.getBoolean("is_active")
        );

        // Set category if available
        String categoryName = rs.getString("category_name");
        if (categoryName != null) {
            Category category = new Category();
            category.setCategoryId(rs.getInt("category_id"));
            category.setCategoryName(categoryName);
            category.setDescription(rs.getString("category_description"));
            product.setCategory(category);
        }

        return product;
    }
}
