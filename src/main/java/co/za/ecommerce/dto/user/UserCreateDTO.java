package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO extends EntityDTO {

    /**
     * User full name.
     */
    @NotBlank
    private String name;

    /**
     * User email address.
     */
    @NotBlank
    @Email
    private String email;

    /**
     * User phone number.
     */
    @NotBlank
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits long")
    private String phone;

    /**
     * User password.
     */
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\\\S+$).{8,20}$")
    private char[] pwd;

    /**
     * User roles
     */
    @NotBlank
    private List<String> roles;
}
