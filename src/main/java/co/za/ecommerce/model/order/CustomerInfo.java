package co.za.ecommerce.model.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerInfo {
    private String firstName;
    private String lastName;
    private String number;
    private String email;
}
