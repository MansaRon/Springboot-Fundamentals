package co.za.ecommerce.model.order;

import co.za.ecommerce.model.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Address extends Entity {
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
