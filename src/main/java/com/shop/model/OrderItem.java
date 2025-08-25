package com.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItem model class representing items within an order
 */
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private Product product;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private LocalDateTime createdAt;

    // Default constructor
    public OrderItem() {
        this.createdAt = LocalDateTime.now();
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }

    // Constructor with essential parameters
    public OrderItem(int orderId, int productId, int quantity, BigDecimal unitPrice) {
        this();
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }

    // Constructor with all parameters
    public OrderItem(int orderItemId, int orderId, int productId, int quantity,
                    BigDecimal unitPrice, BigDecimal lineTotal, LocalDateTime createdAt) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getProductId();
            this.unitPrice = product.getPrice();
            calculateLineTotal();
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateLineTotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public void calculateLineTotal() {
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
        calculateLineTotal();
    }

    public void decreaseQuantity(int amount) {
        if (amount >= quantity) {
            throw new IllegalArgumentException("Cannot decrease quantity by more than current quantity");
        }
        this.quantity -= amount;
        calculateLineTotal();
    }

    @Override
    public String toString() {
        String productName = product != null ? product.getProductName() : "Product #" + productId;
        return quantity + "x " + productName + " @ " + unitPrice + " = " + lineTotal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItem orderItem = (OrderItem) obj;
        return orderItemId == orderItem.orderItemId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(orderItemId);
    }
}
