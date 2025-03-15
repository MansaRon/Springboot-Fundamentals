package co.za.ecommerce.dto.order;

public enum PaymentStatus {
    AUTHORIZED("AUTHORIZED"),
    CAPTURED("CAPTURED"),
    DECLINED("DECLINED"),
    FAILED("FAILED"),
    PENDING("PENDING"),
    REFUNDED("REFUNDED"),
    VOIDED("VOIDED");

    PaymentStatus(String description) {}
}
