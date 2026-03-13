package co.za.ecommerce.dto.order;

public enum PaymentStatus {
    FAILED("FAILED"),
    PENDING("PENDING"),
    COMPLETED("COMPLETED");

    PaymentStatus(String description) {}
}
