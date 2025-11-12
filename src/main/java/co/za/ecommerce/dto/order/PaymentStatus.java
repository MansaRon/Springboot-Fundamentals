package co.za.ecommerce.dto.order;

public enum PaymentStatus {
    INITIATED("INITIATED"),
    AUTHORIZED("AUTHORIZED"),
    CAPTURED("CAPTURED"),
    DECLINED("DECLINED"),
    FAILED("FAILED"),
    PENDING("PENDING"),
    REFUNDED("REFUNDED"),
    VOIDED("VOIDED"),
    COMPLETED("COMPLETED");

    PaymentStatus(String description) {}
}
