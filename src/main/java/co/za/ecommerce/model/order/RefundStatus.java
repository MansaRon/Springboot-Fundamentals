package co.za.ecommerce.model.order;

/**
 * Lifecycle of a refund. In this system refunds settle immediately (there is no external
 * gateway to reconcile), so an initiated refund is recorded as {@code COMPLETED}; the
 * {@code PENDING}/{@code FAILED} states exist for a future real-gateway integration.
 */
public enum RefundStatus {
    PENDING,
    COMPLETED,
    FAILED
}
