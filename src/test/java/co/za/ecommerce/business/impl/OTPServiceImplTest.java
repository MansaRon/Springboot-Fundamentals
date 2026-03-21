package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.otp.OTPResponseDTO;
import co.za.ecommerce.exception.OTPException;
import co.za.ecommerce.model.OtpStore;
import co.za.ecommerce.repository.OTPRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OTPService Tests")
class OTPServiceImplTest {

    @Mock private OTPRepository otpRepository;
    @InjectMocks private OTPServiceImpl otpService;

    private static final String PHONE_NUMBER = "1234567890";
    private static final String VALID_OTP = "98765";

    private OtpStore validOtpStore;
    private OtpStore expiredOtpStore;

    @BeforeEach
    void setUp() {
        validOtpStore = OtpStore.builder()
                .id(new ObjectId())
                .phoneNumber(PHONE_NUMBER)
                .otp(VALID_OTP)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .retryAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        expiredOtpStore = OtpStore.builder()
                .id(new ObjectId())
                .phoneNumber(PHONE_NUMBER)
                .otp(VALID_OTP)
                .expiryTime(LocalDateTime.now().minusMinutes(10))
                .retryAttempts(0)
                .createdAt(LocalDateTime.now().minusMinutes(15))
                .build();
    }

    @Nested
    @DisplayName("generateOTP")
    class GenerateOTP {

        @Test
        @DisplayName("shouldGenerateAndSaveOTPWhenNoExistingOTPFound")
        void shouldGenerateAndSaveOTPWhenNoExistingOTPFound() {
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());
            when(otpRepository.save(any(OtpStore.class))).thenReturn(validOtpStore);

            OTPResponseDTO result = otpService.generateOTP(PHONE_NUMBER);

            assertThat(result).isNotNull();
            assertThat(result.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
            assertThat(result.getExpiryMinutes()).isEqualTo(10);

            ArgumentCaptor<OtpStore> captor = ArgumentCaptor.forClass(OtpStore.class);
            verify(otpRepository).save(captor.capture());
            OtpStore saved = captor.getValue();
            assertThat(saved.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
            assertThat(saved.getOtp()).hasSize(6);
            assertThat(saved.getRetryAttempts()).isZero();
            assertThat(saved.getExpiryTime()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("shouldDeleteExistingOTPBeforeGeneratingNewOne")
        void shouldDeleteExistingOTPBeforeGeneratingNewOne() {
            // Arrange
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(validOtpStore));
            when(otpRepository.save(any(OtpStore.class))).thenReturn(validOtpStore);

            // Act
            otpService.generateOTP(PHONE_NUMBER);

            // Assert
            verify(otpRepository).deleteByPhoneNumber(PHONE_NUMBER);
            verify(otpRepository).save(any(OtpStore.class));
        }

        @Test
        @DisplayName("shouldGenerateSixDigitOTP")
        void shouldGenerateSixDigitOTP() {
            // Arrange
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());
            when(otpRepository.save(any(OtpStore.class))).thenReturn(validOtpStore);

            // Act
            otpService.generateOTP(PHONE_NUMBER);

            // Assert
            ArgumentCaptor<OtpStore> captor = ArgumentCaptor.forClass(OtpStore.class);
            verify(otpRepository).save(captor.capture());
            String generatedOtp = captor.getValue().getOtp();
            assertThat(generatedOtp).hasSize(6);
            assertThat(generatedOtp).matches("\\d{6}");
        }
    }

    @Nested
    @DisplayName("cleanupExpiredOtps")
    class CleanupExpiredOtps {

        @Test
        @DisplayName("shouldDeleteAllExpiredOTPsWhenExpiredOTPsExist")
        void shouldDeleteAllExpiredOTPsWhenExpiredOTPsExist() {
            // Arrange
            List<OtpStore> expiredList = List.of(expiredOtpStore);
            when(otpRepository.findByExpiryTimeBefore(any(LocalDateTime.class))).thenReturn(expiredList);

            // Act
            otpService.cleanupExpiredOtps();

            // Assert
            verify(otpRepository).deleteAll(expiredList);
        }

        @Test
        @DisplayName("shouldNotCallDeleteAllWhenNoExpiredOTPsExist")
        void shouldNotCallDeleteAllWhenNoExpiredOTPsExist() {
            // Arrange
            when(otpRepository.findByExpiryTimeBefore(any(LocalDateTime.class))).thenReturn(List.of());

            // Act
            otpService.cleanupExpiredOtps();

            // Assert
            verify(otpRepository, never()).deleteAll(anyList());
        }
    }

    @Nested
    @DisplayName("hasValidOTP")
    class HasValidOTP {

        @Test
        @DisplayName("shouldReturnTrueWhenValidNonExpiredOTPExists")
        void shouldReturnTrueWhenValidNonExpiredOTPExists() {
            // Arrange
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(validOtpStore));

            // Act
            boolean result = otpService.hasValidOTP(PHONE_NUMBER);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("shouldReturnFalseWhenOTPIsExpired")
        void shouldReturnFalseWhenOTPIsExpired() {
            // Arrange
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(expiredOtpStore));

            // Act
            boolean result = otpService.hasValidOTP(PHONE_NUMBER);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("shouldReturnFalseWhenNoOTPExistsForPhoneNumber")
        void shouldReturnFalseWhenNoOTPExistsForPhoneNumber() {
            // Arrange
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());

            // Act
            boolean result = otpService.hasValidOTP(PHONE_NUMBER);

            // Assert
            assertThat(result).isFalse();
        }

    }

    @Test
    void validateOTPInternal() {
    }

    @Nested
    @DisplayName("resendOTP")
    class ResendOTP {

        @Test
        @DisplayName("shouldResendOTPByDelegatingToGenerateOTP")
        void shouldResendOTPByDelegatingToGenerateOTP() {
            // Arrange
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());
            when(otpRepository.save(any(OtpStore.class))).thenReturn(validOtpStore);

            // Act
            OTPResponseDTO result = otpService.resendOTP(PHONE_NUMBER);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
            verify(otpRepository).save(any(OtpStore.class));
        }

        @Test
        @DisplayName("shouldDeleteOldOTPWhenResending")
        void shouldDeleteOldOTPWhenResending() {
            // Arrange
            when(otpRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(validOtpStore));
            when(otpRepository.save(any(OtpStore.class))).thenReturn(validOtpStore);

            // Act
            otpService.resendOTP(PHONE_NUMBER);

            // Assert
            verify(otpRepository).deleteByPhoneNumber(PHONE_NUMBER);
            verify(otpRepository).save(any(OtpStore.class));
        }
    }
}