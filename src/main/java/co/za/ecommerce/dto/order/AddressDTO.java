package co.za.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Postal code is required")
    private String postalCode;
}
