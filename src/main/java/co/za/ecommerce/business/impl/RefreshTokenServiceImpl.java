package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.RefreshTokenService;
import co.za.ecommerce.dto.user.TokenRefreshResponse;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.model.RefreshToken;
import co.za.ecommerce.repository.RefreshTokenRepository;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiry(Date.from(Instant.now().plus(Duration.ofDays(7))));

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ClientException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (storedToken.getExpiry().before(new Date())) {
            refreshTokenRepository.delete(storedToken);
            throw new ClientException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        refreshTokenRepository.delete(storedToken);
        RefreshToken newRefreshToken = createRefreshToken(storedToken.getUserId());

        String newAccessToken = jwtTokenProvider.generateToken(userRepository.findById(new ObjectId(storedToken.getUserId())).get().getEmail());

        return new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());
    }
}
