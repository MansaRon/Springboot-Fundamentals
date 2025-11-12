package co.za.ecommerce.model.order;

public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    ACCEPTED("Accepted"),
    CONFIRMED("CONFIRMED"),
    PAID("PAID");

    OrderStatus(String description) {
    }
}
