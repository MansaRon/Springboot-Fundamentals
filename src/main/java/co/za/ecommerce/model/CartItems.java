package co.za.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CartItems {

    @DBRef
    private Product product;

    private int quantity;
}
