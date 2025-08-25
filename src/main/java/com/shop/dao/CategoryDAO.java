package com.shop.dao;

import com.shop.database.DatabaseConfig;
import com.shop.model.Category;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Category operations
 */
public class CategoryDAO {
    private final DatabaseConfig dbConfig;

    public CategoryDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public Category save(Category category) throws SQLException {
        if (category.getCategoryId() == 0) {
            return insert(category);
        } else {
            return update(category);
        }
    }

    private Category insert(Category category) throws SQLException {
        String sql = """
            INSERT INTO categories (category_name, description, created_at, updated_at, is_active)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(category.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(category.getUpdatedAt()));
            stmt.setBoolean(5, category.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setCategoryId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
        return category;
    }

    private Category update(Category category) throws SQLException {
        String sql = """
            UPDATE categories SET category_name = ?, description = ?, updated_at = ?, is_active = ?
            WHERE category_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(4, category.isActive());
            stmt.setInt(5, category.getCategoryId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating category failed, no rows affected.");
            }
        }
        return category;
    }

    public Optional<Category> findById(int categoryId) throws SQLException {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCategory(rs));
            }
        }
        return Optional.empty();
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT * FROM categories ORDER BY category_name";
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        }
        return categories;
    }

    public List<Category> findActive() throws SQLException {
        String sql = "SELECT * FROM categories WHERE is_active = TRUE ORDER BY category_name";
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        }
        return categories;
    }

    public Optional<Category> findByName(String categoryName) throws SQLException {
        String sql = "SELECT * FROM categories WHERE category_name = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCategory(rs));
            }
        }
        return Optional.empty();
    }

    public boolean delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean softDelete(int categoryId) throws SQLException {
        String sql = "UPDATE categories SET is_active = FALSE, updated_at = ? WHERE category_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existsByName(String categoryName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime(),
                rs.getBoolean("is_active")
        );
    }
}
