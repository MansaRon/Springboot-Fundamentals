package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.OTPAPI;
import co.za.ecommerce.dto.api.OTPApiResource;
import co.za.ecommerce.dto.otp.OTPGenerateDTO;
import co.za.ecommerce.dto.otp.OTPValidateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/otp")
public class OTPAPIImpl extends API implements OTPAPI {

    @Override
    @PostMapping("/generate")
    public ResponseEntity<OTPApiResource> generateOTP(@Valid @RequestBody OTPGenerateDTO request) {
        log.trace("public ResponseEntity<OTPApiResource> generateOTP(@Valid @RequestBody OTPGenerateDTO request)");
        return ResponseEntity.ok(
                OTPApiResource.builder()
                        .timestamp(Instant.now())
                        .data(otpService.generateOTP(request.getPhoneNumber()))
                        .message("OTP sent successfully. Valid for 5 minutes.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @PostMapping("/resend")
    public ResponseEntity<OTPApiResource> resendOTP(@Valid @RequestBody OTPGenerateDTO request) {
        log.trace("public ResponseEntity<OTPApiResource> resendOTP(@Valid @RequestBody OTPGenerateDTO request)");
        return ResponseEntity.ok(
                OTPApiResource.builder()
                        .timestamp(Instant.now())
                        .data(otpService.resendOTP(request.getPhoneNumber()))
                        .message("New OTP sent successfully. Valid for 5 minutes.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<OTPApiResource> validateOTP(@Valid @RequestBody OTPValidateDTO request) {
        log.trace("public ResponseEntity<OTPApiResource> validateOTP(@Valid @RequestBody OTPValidateDTO request)");
        return ResponseEntity.ok(
                OTPApiResource.builder()
                        .timestamp(Instant.now())
                        .data(otpService.validateOTP(request.getPhoneNumber(), request.getOtp()))
                        .message("OTP validated successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
