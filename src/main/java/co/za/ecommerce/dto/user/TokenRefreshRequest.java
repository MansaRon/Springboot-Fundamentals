package co.za.ecommerce.dto.user;

import jakarta.validation.constraints.NotBlank;
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
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
