package co.za.ecommerce.dto.user;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutDTO {
    private String refreshToken;
}
