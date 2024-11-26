package co.za.ecommerce.factories;

import co.za.ecommerce.dto.user.UserCreateDTO;

import java.util.Arrays;

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
}
