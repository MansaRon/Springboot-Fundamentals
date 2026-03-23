package co.za.ecommerce.utils;

import org.junit.jupiter.api.*;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenerateUtil Tests")
class GenerateUtilTest {
    @Test
    @DisplayName("shouldGenerateTransactionIdWithCorrectFormat")
    void shouldGenerateTransactionIdWithCorrectFormat() {
        String txnId = GenerateUtil.generateTransactionId();

        assertThat(txnId).isNotNull().isNotEmpty();
        assertThat(txnId).startsWith("TXN-");
        assertThat(txnId).matches("TXN-\\d{8}-[A-F0-9]{8}");
    }

    @Test
    @DisplayName("shouldGenerateOrderNumberWithCorrectFormat")
    void shouldGenerateOrderNumberWithCorrectFormat() {
        String orderNumber = GenerateUtil.generateOrderNumber();

        assertThat(orderNumber).isNotNull().isNotEmpty();
        assertThat(orderNumber).startsWith("ORD-");
        assertThat(orderNumber).matches("ORD-\\d{8}-[A-Z0-9]{6}");
    }

    @Test
    @DisplayName("shouldGenerateTransactionIdContainingTodaysDate")
    void shouldGenerateTransactionIdContainingTodaysDate() {
        String todayDate = DateUtil.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String txnId = GenerateUtil.generateTransactionId();

        assertThat(txnId).contains(todayDate);
    }

    @Test
    @DisplayName("shouldGenerateOrderNumberContainingTodaysDate")
    void shouldGenerateOrderNumberContainingTodaysDate() {
        String todayDate = DateUtil.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String orderNumber = GenerateUtil.generateOrderNumber();

        assertThat(orderNumber).contains(todayDate);
    }

    @Test
    @DisplayName("shouldGenerateRandomAlphanumericWithSixUppercaseChars")
    void shouldGenerateRandomAlphanumericWithSixUppercaseChars() {
        String result = GenerateUtil.generateRandomAlphanumeric();

        assertThat(result).hasSize(6);
        assertThat(result).matches("[A-Z0-9]{6}");
    }

    @Test
    @DisplayName("shouldGenerateOTPAsSixDigitNumber")
    void shouldGenerateOTPAsSixDigitNumber() {
        String otp = GenerateUtil.generateOTP();

        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}");

        int otpValue = Integer.parseInt(otp);
        assertThat(otpValue).isBetween(100000, 999999);
    }
}