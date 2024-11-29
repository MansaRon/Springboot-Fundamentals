package co.za.ecommerce.business;

public interface OTPService {
    String generateOTP(String id);
    boolean validateOTP(String emailOrPhone, String otp);
}
