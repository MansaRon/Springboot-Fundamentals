package co.za.ecommerce.model.checkout;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "payment_methods")
public class PaymentMethod {
    /**
     * The type of payment being made E.g., Credit Card, PayPal
     */
    private String type;

    /**
     * The provider of the payment E.g., Visa, MasterCard
     */
    private String provider; //

    /**
     * The last 4 digits of the card
     */
    private String lastFourDigits;
}
