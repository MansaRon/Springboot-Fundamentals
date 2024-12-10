package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.DTO;
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
public class ResetPwdDTO extends EntityDTO {
    public String username;
    public boolean pwdReset = false;
}
