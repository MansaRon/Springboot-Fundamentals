package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.OTPService;
import co.za.ecommerce.exception.OTPException;
import co.za.ecommerce.model.OtpStore;
import co.za.ecommerce.repository.OTPRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class OTPServiceImpl implements OTPService {

    @Autowired
    private OTPRepository otpRepository;

    @Override
    public String generateOTP(String phoneNumber) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // Generate 6-digit OTP
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5); // 5-minute validity

        OtpStore otpStore = OtpStore
                .builder()
                .phoneNumber(phoneNumber)
                .otp(otp)
                .expiryTime(expiryTime)
                .retryAttempts(0)
                .build();

        otpRepository.save(otpStore);

        return otp;
    }

    @Override
    public boolean validateOTP(String phoneNumber, String otp) {

        OtpStore otpStore = otpRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new OTPException(HttpStatus.BAD_REQUEST, "OTP not found")
                );

        if (otpStore.isExpired()) {
            otpRepository.deleteById(phoneNumber); // Cleanup expired OTP
            throw new OTPException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        if (!otpStore.getOtp().equals(otp)) {
            otpStore.setRetryAttempts(otpStore.getRetryAttempts() + 1);
            otpRepository.save(otpStore);

            if (otpStore.getRetryAttempts() >= 3) { // Limit retries
                otpRepository.deleteById(phoneNumber);
                throw new OTPException(HttpStatus.BAD_REQUEST, "Too many failed attempts. OTP invalidated.");
            }

            throw new OTPException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        otpRepository.deleteById(phoneNumber); // Cleanup OTP after successful validation
        return true;
    }

    public void cleanupExpiredOtps() {
        List<OtpStore> expiredOtps = otpRepository
                .findAll()
                .stream()
                .filter(OtpStore::isExpired)
                .toList();
        otpRepository.deleteAll(expiredOtps);
    }
}
