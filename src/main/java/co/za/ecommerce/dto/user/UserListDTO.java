package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Admin-facing projection of a user for listing screens.
 * Intentionally omits password, tokens and OTP material.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO extends EntityDTO {

    /**
     * User full name.
     */
    private String name;

    /**
     * User email address.
     */
    private String email;

    /**
     * User phone number.
     */
    private String phone;

    /**
     * Account status.
     */
    private String status;

    /**
     * Roles assigned to the user.
     */
    private Set<String> roles;
}
