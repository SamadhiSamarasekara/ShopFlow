package com.shop.dao;

import com.shop.database.DatabaseConfig;
import com.shop.model.OrderItem;
import com.shop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for OrderItem CRUD operations
 */
public class OrderItemDAO {
    private final DatabaseConfig dbConfig;

    public OrderItemDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // CREATE - Insert new order item
    public OrderItem save(OrderItem orderItem) throws SQLException {
        if (orderItem.getOrderItemId() == 0) {
            return insert(orderItem);
        } else {
            return update(orderItem);
        }
    }

    private OrderItem insert(OrderItem orderItem) throws SQLException {
        String sql = """
            INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, orderItem.getOrderId());
            stmt.setInt(2, orderItem.getProductId());
            stmt.setInt(3, orderItem.getQuantity());
            stmt.setBigDecimal(4, orderItem.getUnitPrice());
            stmt.setBigDecimal(5, orderItem.getLineTotal());
            stmt.setTimestamp(6, Timestamp.valueOf(orderItem.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderItem.setOrderItemId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating order item failed, no ID obtained.");
                }
            }
        }
        return orderItem;
    }

    // UPDATE - Update existing order item
    private OrderItem update(OrderItem orderItem) throws SQLException {
        String sql = """
            UPDATE order_items SET order_id = ?, product_id = ?, quantity = ?, 
                                  unit_price = ?, line_total = ?
            WHERE order_item_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderItem.getOrderId());
            stmt.setInt(2, orderItem.getProductId());
            stmt.setInt(3, orderItem.getQuantity());
            stmt.setBigDecimal(4, orderItem.getUnitPrice());
            stmt.setBigDecimal(5, orderItem.getLineTotal());
            stmt.setInt(6, orderItem.getOrderItemId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating order item failed, no rows affected.");
            }
        }
        return orderItem;
    }

    // READ - Find order item by ID
    public Optional<OrderItem> findById(int orderItemId) throws SQLException {
        String sql = """
            SELECT oi.*, p.product_name, p.sku
            FROM order_items oi
            LEFT JOIN products p ON oi.product_id = p.product_id
            WHERE oi.order_item_id = ?
        """;

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderItemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToOrderItem(rs));
            }
        }
        return Optional.empty();
    }

    // READ - Find all order items by order ID
    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        String sql = """
            SELECT oi.*, p.product_name, p.sku
            FROM order_items oi
            LEFT JOIN products p ON oi.product_id = p.product_id
            WHERE oi.order_id = ?
            ORDER BY oi.order_item_id
        """;

        List<OrderItem> orderItems = new ArrayList<>();
        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orderItems.add(mapResultSetToOrderItem(rs));
            }
        }
        return orderItems;
    }

    // CREATE/UPDATE - Save multiple order items for an order (used in transactions)
    public void saveOrderItems(int orderId, List<OrderItem> orderItems, Connection conn) throws SQLException {
        // First, delete existing order items
        deleteByOrderId(orderId, conn);
        
        // Then insert new order items
        String sql = """
            INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : orderItems) {
                item.setOrderId(orderId);
                
                stmt.setInt(1, item.getOrderId());
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getUnitPrice());
                stmt.setBigDecimal(5, item.getLineTotal());
                stmt.setTimestamp(6, Timestamp.valueOf(item.getCreatedAt()));
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }

    // DELETE - Delete order item
    public boolean delete(int orderItemId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_item_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Delete all order items for an order
    public boolean deleteByOrderId(int orderId) throws SQLException {
        return deleteByOrderId(orderId, dbConfig.getConnection());
    }

    private boolean deleteByOrderId(int orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() >= 0; // Can be 0 if no items exist
        }
    }

    // UPDATE - Update quantity for order item
    public boolean updateQuantity(int orderItemId, int newQuantity) throws SQLException {
        String sql = "UPDATE order_items SET quantity = ?, line_total = unit_price * ? WHERE order_item_id = ?";

        try (PreparedStatement stmt = dbConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, newQuantity);
            stmt.setInt(3, orderItemId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Map ResultSet to OrderItem object
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem orderItem = new OrderItem(
                rs.getInt("order_item_id"),
                rs.getInt("order_id"),
                rs.getInt("product_id"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("line_total"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );

        // Set product information if available
        String productName = rs.getString("product_name");
        if (productName != null) {
            Product product = new Product();
            product.setProductId(rs.getInt("product_id"));
            product.setProductName(productName);
            product.setSku(rs.getString("sku"));
            orderItem.setProduct(product);
        }

        return orderItem;
    }
}
