package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO extends EntityDTO {

    /**
     * User full name.
     */
    @NotBlank
    private String name;

    /**
     * User email address.
     */
    @Email
    @NotBlank
    private String email;

    /**
     * User phone number.
     */
    @NotBlank
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits long")
    private String phone;

    /**
     * User access token.
     */
    private String accessToken;


    private String status;

    /**
     * User roles
     */
    private Set<String> role;
}
