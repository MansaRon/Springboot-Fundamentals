package co.za.ecommerce.dto.api;

import co.za.ecommerce.dto.GlobalApiResponse;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class CheckoutDTOApiResource extends GlobalApiResponse {
    private CheckoutDTO data;
}
