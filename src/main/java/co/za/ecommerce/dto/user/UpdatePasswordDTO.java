package co.za.ecommerce.dto.user;

import co.za.ecommerce.dto.base.EntityDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String newPassword;
}
