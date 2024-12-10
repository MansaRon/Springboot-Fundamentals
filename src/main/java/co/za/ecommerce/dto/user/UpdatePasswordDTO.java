package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordDTO extends EntityDTO {
    private String email;
    private String currentPassword;
    private String newPassword;
}
