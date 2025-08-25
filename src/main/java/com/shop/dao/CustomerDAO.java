package com.shop.dao;

import com.shop.database.DatabaseConfig;
import com.shop.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Customer CRUD operations
 */
public class CustomerDAO {
    private final DatabaseConfig dbConfig;

    public CustomerDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // CREATE - Insert new customer
    public Customer save(Customer customer) throws SQLException {
        if (customer.getCustomerId() == 0) {
            return insert(customer);
        } else {
            return update(customer);
        }
    }

    private Customer insert(Customer customer) throws SQLException {
        String sql = """
            INSERT INTO customers (first_name, last_name, email, phone_number, address, 
                                 city, postal_code, country, created_at, updated_at, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getAddress());
            stmt.setString(6, customer.getCity());
            stmt.setString(7, customer.getPostalCode());
            stmt.setString(8, customer.getCountry());
            stmt.setTimestamp(9, Timestamp.valueOf(customer.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(customer.getUpdatedAt()));
            stmt.setBoolean(11, customer.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setCustomerId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
        return customer;
    }

    // UPDATE - Update existing customer
    private Customer update(Customer customer) throws SQLException {
        String sql = """
            UPDATE customers SET first_name = ?, last_name = ?, email = ?, phone_number = ?, 
                               address = ?, city = ?, postal_code = ?, country = ?, 
                               updated_at = ?, is_active = ?
            WHERE customer_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getAddress());
            stmt.setString(6, customer.getCity());
            stmt.setString(7, customer.getPostalCode());
            stmt.setString(8, customer.getCountry());
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(10, customer.isActive());
            stmt.setInt(11, customer.getCustomerId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating customer failed, no rows affected.");
            }
        }
        return customer;
    }

    // READ - Find customer by ID
    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCustomer(rs));
            }
        }
        return Optional.empty();
    }

    // READ - Find all customers
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY first_name, last_name";
        return executeCustomerQuery(sql);
    }

    // READ - Find active customers
    public List<Customer> findActive() throws SQLException {
        String sql = "SELECT * FROM customers WHERE is_active = TRUE ORDER BY first_name, last_name";
        return executeCustomerQuery(sql);
    }

    // READ - Find customer by email
    public Optional<Customer> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCustomer(rs));
            }
        }
        return Optional.empty();
    }

    // READ - Search customers by name
    public List<Customer> searchByName(String searchTerm) throws SQLException {
        String sql = """
            SELECT * FROM customers 
            WHERE (first_name ILIKE ? OR last_name ILIKE ?) AND is_active = TRUE 
            ORDER BY first_name, last_name
        """;

        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    // DELETE - Hard delete customer
    public boolean delete(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Soft delete customer (mark as inactive)
    public boolean softDelete(int customerId) throws SQLException {
        String sql = "UPDATE customers SET is_active = FALSE, updated_at = ? WHERE customer_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Utility method to check if email exists
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE email = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Helper method to execute customer queries
    private List<Customer> executeCustomerQuery(String sql) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    // Map ResultSet to Customer object
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("postal_code"),
                rs.getString("country"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime(),
                rs.getBoolean("is_active")
        );
    }
}
