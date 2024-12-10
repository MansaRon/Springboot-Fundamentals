package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO extends EntityDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
