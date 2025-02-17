package co.za.ecommerce.model.checkout;

public enum CheckoutStatus {
    PENDING("Pending"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    CheckoutStatus(String description) {}
}
