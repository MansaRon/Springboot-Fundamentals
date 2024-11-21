package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO extends EntityDTO {

    @NotBlank
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits long")
    private String phone;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;
}
