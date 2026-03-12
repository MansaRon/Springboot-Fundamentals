package co.za.ecommerce.factories;

import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.dto.user.UserCreateDTO;
import co.za.ecommerce.utils.DateUtil;

import java.util.Arrays;
import java.util.List;

public final class DTOFactory {

    public static UserCreateDTO userCreateDTO() {
        return UserCreateDTO.builder()
                .name("user")
                .email("test@gmail.com")
                .phone("0787430054")
                .pwd("0987654321")
                .roles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                .build();
    }

    public static ProductDTO createProductDTO() {
        return ProductDTO.builder()
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .category("Category test")
                .description("Category Description")
                .price(15.00)
                .rate("4.5")
                .title("Product Title")
                .quantity(15)
                .images(List.of())
                .build();
    }
}
