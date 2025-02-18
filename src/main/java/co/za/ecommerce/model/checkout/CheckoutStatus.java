package co.za.ecommerce.model.checkout;

public enum CheckoutStatus {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    CheckoutStatus(String description) {}
}
