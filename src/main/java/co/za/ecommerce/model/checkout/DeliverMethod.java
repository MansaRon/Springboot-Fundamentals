package co.za.ecommerce.model.checkout;

public enum DeliverMethod {
    DHL("DHL_FAST_DELIVERY"),
    FedEx("FEDEX_FREE_DELIVERY"),
    Express("EXPRESS_DELIVERY");

    DeliverMethod(String description) {}
}
