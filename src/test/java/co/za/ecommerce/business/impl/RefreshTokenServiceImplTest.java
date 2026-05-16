package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.user.TokenRefreshResponse;
import co.za.ecommerce.exception.ClientException;
import co.za.ecommerce.model.RefreshToken;
import co.za.ecommerce.model.User;
import co.za.ecommerce.repository.RefreshTokenRepository;
import co.za.ecommerce.repository.UserRepository;
import co.za.ecommerce.security.JwtTokenProvider;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Tests")
class RefreshTokenServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;
    @InjectMocks private RefreshTokenServiceImpl refreshTokenService;

    private ObjectId userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        user = TestDataBuilder.buildUser(userId);
    }

    @Nested
    @DisplayName("CreateRefreshToken")
    class CreateRefreshToken {

        @Test
        @DisplayName("shouldCreateAndPersistRefreshToken")
        void shouldCreateAndPersistRefreshToken() {
            RefreshToken saved = new RefreshToken();
            saved.setToken("saved-token");
            saved.setUserId(userId.toHexString());
            saved.setExpiry(Date.from(Instant.now().plusSeconds(600000)));

            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(saved);

            RefreshToken result = refreshTokenService.createRefreshToken(userId.toHexString());

            assertThat(result.getToken()).isEqualTo("saved-token");
            assertThat(result.getUserId()).isEqualTo(userId.toHexString());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("RefreshAccessToken")
    class RefreshAccessToken {

        @Test
        @DisplayName("shouldReturnNewTokensWhenRefreshTokenIsValid")
        void shouldReturnNewTokensWhenRefreshTokenIsValid() {
            RefreshToken storedToken = new RefreshToken();
            storedToken.setToken("valid-refresh");
            storedToken.setUserId(userId.toHexString());
            storedToken.setExpiry(Date.from(Instant.now().plusSeconds(600000)));

            RefreshToken newRefreshToken = new RefreshToken();
            newRefreshToken.setToken("new-refresh");
            newRefreshToken.setExpiry(Date.from(Instant.now().plusSeconds(600000)));

            when(refreshTokenRepository.findByToken("valid-refresh")).thenReturn(Optional.of(storedToken));
            when(refreshTokenRepository.save(any())).thenReturn(newRefreshToken);
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(user));
            when(jwtTokenProvider.generateToken(anyString())).thenReturn("new-access-token");

            TokenRefreshResponse response = refreshTokenService.refreshAccessToken("valid-refresh");

            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
            verify(refreshTokenRepository).delete(storedToken);
        }

        @Test
        @DisplayName("shouldThrowWhenRefreshTokenNotFound")
        void shouldThrowWhenRefreshTokenNotFound() {
            when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("unknown"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("shouldThrowAndDeleteWhenRefreshTokenIsExpired")
        void shouldThrowAndDeleteWhenRefreshTokenIsExpired() {
            RefreshToken expiredToken = new RefreshToken();
            expiredToken.setToken("expired");
            expiredToken.setUserId(userId.toHexString());
            expiredToken.setExpiry(Date.from(Instant.now().minusSeconds(10)));

            when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("expired"))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining("Refresh token expired");

            verify(refreshTokenRepository).delete(expiredToken);
        }
    }
}