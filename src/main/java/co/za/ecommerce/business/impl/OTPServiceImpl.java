package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.OTPService;
import co.za.ecommerce.dto.otp.OTPResponseDTO;
import co.za.ecommerce.exception.OTPException;
import co.za.ecommerce.model.OtpStore;
import co.za.ecommerce.repository.OTPRepository;
import co.za.ecommerce.utils.GenerateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OTPServiceImpl implements OTPService {

    @Autowired
    private OTPRepository otpRepository;

    @Override
    @Transactional
    public OTPResponseDTO generateOTP(String phoneNumber) {
        log.info("Generating OTP for phone number: {}", phoneNumber);

        otpRepository.findByPhoneNumber(phoneNumber)
                .ifPresent(existing -> otpRepository.deleteByPhoneNumber(phoneNumber));

        String otp = GenerateUtil.generateOTP();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(1);

        OtpStore otpStore = OtpStore
                .builder()
                .phoneNumber(phoneNumber)
                .otp(otp)
                .expiryTime(expiryTime)
                .retryAttempts(0)
                .build();

        otpRepository.save(otpStore);

        log.info("OTP generated for: {}. Expires at: {}", phoneNumber, expiryTime);
        log.info("OTP: {}", otp);

        return OTPResponseDTO.builder()
                .phoneNumber(phoneNumber)
                .expiryMinutes(10)
                .build();
    }

    @Override
    public OTPResponseDTO validateOTP(String phoneNumber, String otp) {
        log.info("Validating OTP for phone number: {}", phoneNumber);

        OtpStore otpStore = otpRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new OTPException(HttpStatus.BAD_REQUEST, "OTP not found")
                );

        if (otpStore.isExpired()) {
            otpRepository.deleteById(phoneNumber);
            throw new OTPException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        if (!otpStore.getOtp().equals(otp)) {
            otpStore.setRetryAttempts(otpStore.getRetryAttempts() + 1);

            if (otpStore.getRetryAttempts() >= 3) {
                otpRepository.deleteById(phoneNumber);
                throw new OTPException(HttpStatus.BAD_REQUEST, "Too many failed attempts. OTP invalidated.");
            }
            otpRepository.save(otpStore);

            int remaining = 3 - otpStore.getRetryAttempts();
            throw new OTPException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Invalid OTP. %d attempt(s) remaining.", remaining)
            );
        }

        otpRepository.deleteById(phoneNumber);
        log.info("OTP validated successfully for: {}", phoneNumber);

        return OTPResponseDTO.builder()
                .phoneNumber(phoneNumber)
                .valid(true)
                .build();
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("Running expired OTP cleanup...");
        List<OtpStore> expired = otpRepository.findByExpiryTimeBefore(LocalDateTime.now());
        if (!expired.isEmpty()) {
            otpRepository.deleteAll(expired);
            log.info("Deleted {} expired OTPs", expired.size());
        }
    }

    @Override
    public boolean hasValidOTP(String phoneNumber) {
        return otpRepository.findByPhoneNumber(phoneNumber)
                .map(o -> !o.isExpired())
                .orElse(false);
    }

    @Override
    public boolean validateOTPInternal(String phoneNumber, String otp) {
        validateOTP(phoneNumber, otp);
        return true;
    }

    @Override
    @Transactional
    public OTPResponseDTO resendOTP(String phoneNumber) {
        log.info("Resending OTP for phone number: {}", phoneNumber);
        return generateOTP(phoneNumber);
    }
}
