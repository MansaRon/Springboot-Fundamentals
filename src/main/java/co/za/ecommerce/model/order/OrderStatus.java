package co.za.ecommerce.model.order;

public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    CONFIRMED("Confirmed");

    OrderStatus(String description) {}
}
