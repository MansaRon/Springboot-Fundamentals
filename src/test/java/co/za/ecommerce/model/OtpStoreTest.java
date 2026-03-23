package co.za.ecommerce.model;

import co.za.ecommerce.utils.DateUtil;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OtpStore Test")
class OtpStoreTest {

    @Test
    @DisplayName("shouldReturnFalseWhenExpiryTimeIsInTheFuture")
    void shouldReturnFalseWhenExpiryTimeIsInTheFuture() {
        OtpStore otp = OtpStore.builder()
                .id(new ObjectId())
                .phoneNumber("0821234567")
                .otp("483920")
                .expiryTime(DateUtil.now().plusMinutes(5))
                .retryAttempts(0)
                .build();

        assertThat(otp.isExpired()).isFalse();
    }

    @Test
    @DisplayName("shouldReturnTrueWhenExpiryTimeIsInThePast")
    void shouldReturnTrueWhenExpiryTimeIsInThePast() {
        OtpStore otp = OtpStore.builder()
                .id(new ObjectId())
                .phoneNumber("0821234567")
                .otp("483920")
                .expiryTime(DateUtil.now().minusMinutes(10))
                .retryAttempts(0)
                .build();

        assertThat(otp.isExpired()).isTrue();
    }
}