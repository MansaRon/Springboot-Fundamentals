package co.za.ecommerce.model.checkout;

public enum PaymentMethod {
    CREDIT_CARD("CreditCard"),
    CASH_ON_DELIVERY("CashOnDelivery"),
    EFT("EFT"),
    NOT_SELECTED("NotSelected");

    PaymentMethod(String description) {}
}
