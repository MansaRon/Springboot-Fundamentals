package co.za.ecommerce.model.checkout;

public enum PaymentMethod {
    CREDIT_CARD("CreditCard"),
    PAYPAL("Paypal"),
    BANK_TRANSFER("BankTransfer"),
    CASH_ON_DELIVERY("CashOnDelivery"),
    NOT_SELECTED("NotSelected");

    PaymentMethod(String description) {}
}
