package co.za.ecommerce.model.checkout;

/**
 * Delivery options offered at checkout. This enum is the single source of truth
 * for the delivery contract — label, price (ZAR) and estimated delivery days —
 * consumed by both the checkout total calculation and the delivery-options
 * endpoint the frontend renders from.
 */
public enum DeliverMethod {
    STANDARD("Standard Delivery", 0.0, 5),
    EXPRESS("Express Delivery", 50.0, 2),
    OVERNIGHT("Overnight Delivery", 150.0, 1),
    FREE("Free Delivery", 0.0, 7);

    private final String label;
    private final double price;
    private final int estimatedDays;

    DeliverMethod(String label, double price, int estimatedDays) {
        this.label = label;
        this.price = price;
        this.estimatedDays = estimatedDays;
    }

    public String getLabel() {
        return label;
    }

    public double getPrice() {
        return price;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }
}
