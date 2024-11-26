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
    //@NonNull
    //@Pattern(regexp = "^[a-zA-Z]*$", message = "First Name must not contain numbers or special characters")
    private String name;

    /**
     * User email address.
     */
    //@Email
    //@NonNull
    private String email;

    /**
     * User phone number.
     */
    //@NonNull
    //@Pattern(regexp = "^\\\\d{10}$")
    //@Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits long")
    private String phone;

    /**
     * User password.
     */
    //@NonNull
    //@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\\\S+$).{8,20}$")
    private String pwd;

    /**
     * User roles
     */
    private List<String> roles;
}
