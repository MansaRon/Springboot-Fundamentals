package co.za.ecommerce.business;

import co.za.ecommerce.dto.user.TokenRefreshResponse;
import co.za.ecommerce.model.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String userId);
    TokenRefreshResponse refreshAccessToken(String refreshToken);
}
