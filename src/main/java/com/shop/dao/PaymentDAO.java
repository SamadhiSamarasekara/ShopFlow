package com.shop.dao;

import com.shop.database.DatabaseConfig;
import com.shop.model.Payment;
import com.shop.model.Order;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Payment CRUD operations
 */
public class PaymentDAO {
    private final DatabaseConfig dbConfig;

    public PaymentDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // CREATE/UPDATE - Save payment
    public Payment save(Payment payment) throws SQLException {
        if (payment.getPaymentId() == 0) {
            return insert(payment);
        } else {
            return update(payment);
        }
    }

    // CREATE - Insert new payment
    private Payment insert(Payment payment) throws SQLException {
        String sql = """
            INSERT INTO payments (order_id, payment_method, status, amount, transaction_id, 
                                reference, notes, payment_date, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment.getOrderId());
            stmt.setString(2, payment.getPaymentMethod().name());
            stmt.setString(3, payment.getStatus().name());
            stmt.setBigDecimal(4, payment.getAmount());
            stmt.setString(5, payment.getTransactionId());
            stmt.setString(6, payment.getReference());
            stmt.setString(7, payment.getNotes());
            stmt.setTimestamp(8, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setTimestamp(9, Timestamp.valueOf(payment.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(payment.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating payment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    payment.setPaymentId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating payment failed, no ID obtained.");
                }
            }
        }
        return payment;
    }

    // UPDATE - Update existing payment
    private Payment update(Payment payment) throws SQLException {
        String sql = """
            UPDATE payments SET order_id = ?, payment_method = ?, status = ?, amount = ?, 
                              transaction_id = ?, reference = ?, notes = ?, payment_date = ?, 
                              updated_at = ?
            WHERE payment_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, payment.getOrderId());
            stmt.setString(2, payment.getPaymentMethod().name());
            stmt.setString(3, payment.getStatus().name());
            stmt.setBigDecimal(4, payment.getAmount());
            stmt.setString(5, payment.getTransactionId());
            stmt.setString(6, payment.getReference());
            stmt.setString(7, payment.getNotes());
            stmt.setTimestamp(8, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(10, payment.getPaymentId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating payment failed, no rows affected.");
            }
        }
        return payment;
    }

    // READ - Find payment by ID
    public Optional<Payment> findById(int paymentId) throws SQLException {
        String sql = """
            SELECT p.*, o.order_date, o.total_amount as order_total
            FROM payments p
            LEFT JOIN orders o ON p.order_id = o.order_id
            WHERE p.payment_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPayment(rs));
            }
        }
        return Optional.empty();
    }

    // READ - Find all payments
    public List<Payment> findAll() throws SQLException {
        String sql = """
            SELECT p.*, o.order_date, o.total_amount as order_total
            FROM payments p
            LEFT JOIN orders o ON p.order_id = o.order_id
            ORDER BY p.payment_date DESC
        """;
        
        return executePaymentQuery(sql);
    }

    // READ - Find payments by order ID
    public List<Payment> findByOrderId(int orderId) throws SQLException {
        String sql = """
            SELECT p.*, o.order_date, o.total_amount as order_total
            FROM payments p
            LEFT JOIN orders o ON p.order_id = o.order_id
            WHERE p.order_id = ?
            ORDER BY p.payment_date DESC
        """;

        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    // READ - Find payments by status
    public List<Payment> findByStatus(Payment.PaymentStatus status) throws SQLException {
        String sql = """
            SELECT p.*, o.order_date, o.total_amount as order_total
            FROM payments p
            LEFT JOIN orders o ON p.order_id = o.order_id
            WHERE p.status = ?
            ORDER BY p.payment_date DESC
        """;

        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    // READ - Find payments by payment method
    public List<Payment> findByPaymentMethod(Payment.PaymentMethod method) throws SQLException {
        String sql = """
            SELECT p.*, o.order_date, o.total_amount as order_total
            FROM payments p
            LEFT JOIN orders o ON p.order_id = o.order_id
            WHERE p.payment_method = ?
            ORDER BY p.payment_date DESC
        """;

        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, method.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    // READ - Find payments by date range
    public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = """
            SELECT p.*, o.order_date, o.total_amount as order_total
            FROM payments p
            LEFT JOIN orders o ON p.order_id = o.order_id
            WHERE p.payment_date BETWEEN ? AND ?
            ORDER BY p.payment_date DESC
        """;

        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    // READ - Find payment by transaction ID
    public Optional<Payment> findByTransactionId(String transactionId) throws SQLException {
        String sql = """
            SELECT p.*, o.order_date, o.total_amount as order_total
            FROM payments p
            LEFT JOIN orders o ON p.order_id = o.order_id
            WHERE p.transaction_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, transactionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPayment(rs));
            }
        }
        return Optional.empty();
    }

    // UPDATE - Update payment status
    public boolean updateStatus(int paymentId, Payment.PaymentStatus status) throws SQLException {
        String sql = "UPDATE payments SET status = ?, updated_at = ? WHERE payment_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, paymentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // UPDATE - Set transaction ID (when payment is processed)
    public boolean setTransactionId(int paymentId, String transactionId) throws SQLException {
        String sql = "UPDATE payments SET transaction_id = ?, updated_at = ? WHERE payment_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, transactionId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, paymentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Delete payment
    public boolean delete(int paymentId) throws SQLException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Utility method - Check if order has any payments
    public boolean hasPayments(int orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments WHERE order_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Helper method to execute payment queries
    private List<Payment> executePaymentQuery(String sql) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    // Map ResultSet to Payment object
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment(
                rs.getInt("payment_id"),
                rs.getInt("order_id"),
                Payment.PaymentMethod.valueOf(rs.getString("payment_method")),
                Payment.PaymentStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("amount"),
                rs.getString("transaction_id"),
                rs.getString("reference"),
                rs.getString("notes"),
                rs.getTimestamp("payment_date").toLocalDateTime(),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );

        // Set order information if available
        Timestamp orderDate = rs.getTimestamp("order_date");
        if (orderDate != null) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setOrderDate(orderDate.toLocalDateTime());
            order.setTotalAmount(rs.getBigDecimal("order_total"));
            payment.setOrder(order);
        }

        return payment;
    }
}
