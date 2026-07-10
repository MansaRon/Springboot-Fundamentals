package co.za.ecommerce.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
