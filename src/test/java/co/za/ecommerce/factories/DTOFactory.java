package co.za.ecommerce.factories;

import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.dto.user.UserCreateDTO;

import java.util.Arrays;

import static co.za.ecommerce.utils.DateUtil.now;

public final class DTOFactory {

    public static UserCreateDTO userCreateDTO() {
        return UserCreateDTO.builder()
                .name("Thendo")
                .email("kramashia101@gmail.com")
                .phone("0787430054")
                .pwd("0987654321")
                .roles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                .build();
    }

    public static ProductDTO createProductDTO() {
        return ProductDTO.builder()
                .category("Category test")
                .description("Category Description")
                .imageUrl("http:image.com")
                .price(15.00)
                .rate("4.5")
                .title("Product Title")
                .quantity(15)
                .build();
    }
}
