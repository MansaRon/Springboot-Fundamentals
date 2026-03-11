package co.za.ecommerce.business;

import co.za.ecommerce.dto.otp.OTPResponseDTO;

public interface OTPService {
    OTPResponseDTO generateOTP(String id);
    OTPResponseDTO resendOTP(String phoneNumber);
    OTPResponseDTO validateOTP(String phoneNumber, String otp);
    boolean hasValidOTP(String phoneNumber);
    boolean validateOTPInternal(String phoneNumber, String otp);
}
