package co.za.ecommerce.model.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Address {
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
