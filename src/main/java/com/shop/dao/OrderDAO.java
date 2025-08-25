package com.shop.dao;

import com.shop.database.DatabaseConfig;
import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Order CRUD operations
 */
public class OrderDAO {
    private final DatabaseConfig dbConfig;
    private final OrderItemDAO orderItemDAO;
    private final CustomerDAO customerDAO;

    public OrderDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
        this.orderItemDAO = new OrderItemDAO();
        this.customerDAO = new CustomerDAO();
    }

    // CREATE/UPDATE - Save order
    public Order save(Order order) throws SQLException {
        Connection conn = dbConfig.getConnection();
        conn.setAutoCommit(false);
        
        try {
            Order savedOrder;
            if (order.getOrderId() == 0) {
                savedOrder = insert(order, conn);
            } else {
                savedOrder = update(order, conn);
            }
            
            // Save order items
            orderItemDAO.saveOrderItems(savedOrder.getOrderId(), order.getOrderItems(), conn);
            
            conn.commit();
            return savedOrder;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // CREATE - Insert new order
    private Order insert(Order order, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO orders (customer_id, order_date, status, subtotal, tax_amount, 
                              discount_amount, total_amount, notes, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getCustomerId());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            stmt.setString(3, order.getStatus().name());
            stmt.setBigDecimal(4, order.getSubtotal());
            stmt.setBigDecimal(5, order.getTaxAmount());
            stmt.setBigDecimal(6, order.getDiscountAmount());
            stmt.setBigDecimal(7, order.getTotalAmount());
            stmt.setString(8, order.getNotes());
            stmt.setTimestamp(9, Timestamp.valueOf(order.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(order.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setOrderId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
        return order;
    }

    // UPDATE - Update existing order
    private Order update(Order order, Connection conn) throws SQLException {
        String sql = """
            UPDATE orders SET customer_id = ?, order_date = ?, status = ?, subtotal = ?, 
                            tax_amount = ?, discount_amount = ?, total_amount = ?, notes = ?, 
                            updated_at = ?
            WHERE order_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getCustomerId());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            stmt.setString(3, order.getStatus().name());
            stmt.setBigDecimal(4, order.getSubtotal());
            stmt.setBigDecimal(5, order.getTaxAmount());
            stmt.setBigDecimal(6, order.getDiscountAmount());
            stmt.setBigDecimal(7, order.getTotalAmount());
            stmt.setString(8, order.getNotes());
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(10, order.getOrderId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating order failed, no rows affected.");
            }
        }
        return order;
    }

    // READ - Find order by ID with items
    public Optional<Order> findById(int orderId) throws SQLException {
        String sql = """
            SELECT o.*, c.first_name, c.last_name, c.email
            FROM orders o 
            LEFT JOIN customers c ON o.customer_id = c.customer_id 
            WHERE o.order_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Load order items
                order.setOrderItems(orderItemDAO.findByOrderId(orderId));
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    // READ - Find all orders
    public List<Order> findAll() throws SQLException {
        String sql = """
            SELECT o.*, c.first_name, c.last_name, c.email
            FROM orders o 
            LEFT JOIN customers c ON o.customer_id = c.customer_id 
            ORDER BY o.order_date DESC
        """;
        
        return executeOrderQuery(sql);
    }

    // READ - Find orders by customer
    public List<Order> findByCustomerId(int customerId) throws SQLException {
        String sql = """
            SELECT o.*, c.first_name, c.last_name, c.email
            FROM orders o 
            LEFT JOIN customers c ON o.customer_id = c.customer_id 
            WHERE o.customer_id = ? 
            ORDER BY o.order_date DESC
        """;

        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }

    // READ - Find orders by status
    public List<Order> findByStatus(Order.OrderStatus status) throws SQLException {
        String sql = """
            SELECT o.*, c.first_name, c.last_name, c.email
            FROM orders o 
            LEFT JOIN customers c ON o.customer_id = c.customer_id 
            WHERE o.status = ? 
            ORDER BY o.order_date DESC
        """;

        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }

    // READ - Find orders by date range
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = """
            SELECT o.*, c.first_name, c.last_name, c.email
            FROM orders o 
            LEFT JOIN customers c ON o.customer_id = c.customer_id 
            WHERE o.order_date BETWEEN ? AND ? 
            ORDER BY o.order_date DESC
        """;

        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }

    // UPDATE - Update order status
    public boolean updateStatus(int orderId, Order.OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE order_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Delete order (will cascade to order items)
    public boolean delete(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to execute order queries
    private List<Order> executeOrderQuery(String sql) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }

    // Map ResultSet to Order object
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order(
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                rs.getTimestamp("order_date").toLocalDateTime(),
                Order.OrderStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("tax_amount"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("total_amount"),
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );

        // Set customer information if available
        String firstName = rs.getString("first_name");
        if (firstName != null) {
            Customer customer = new Customer();
            customer.setCustomerId(rs.getInt("customer_id"));
            customer.setFirstName(firstName);
            customer.setLastName(rs.getString("last_name"));
            customer.setEmail(rs.getString("email"));
            order.setCustomer(customer);
        }

        return order;
    }
}
