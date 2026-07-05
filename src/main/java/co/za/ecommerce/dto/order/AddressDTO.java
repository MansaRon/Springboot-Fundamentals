package co.za.ecommerce.dto.order;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
