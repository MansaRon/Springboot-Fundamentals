package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.coupon.CouponDTO;
import co.za.ecommerce.exception.CouponException;
import co.za.ecommerce.model.Coupon;
import co.za.ecommerce.repository.CouponRepository;
import co.za.ecommerce.utils.DateUtil;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService Tests")
class CouponServiceImplTest {

    @Mock private CouponRepository couponRepository;
    @InjectMocks private CouponServiceImpl couponService;

    private Coupon coupon(double amount, double minSpend, int limit, int used, boolean active, boolean expired) {
        return Coupon.builder()
                .id(new ObjectId())
                .code("SAVE50")
                .amount(amount)
                .minSpend(minSpend)
                .usageLimit(limit)
                .usedCount(used)
                .active(active)
                .expiresAt(expired ? DateUtil.now().minusDays(1) : DateUtil.now().plusDays(1))
                .build();
    }

    @Nested
    @DisplayName("validateForSubtotal")
    class Validate {

        @Test
        @DisplayName("returns the coupon when all rules pass")
        void returnsCouponWhenValid() {
            when(couponRepository.findByCodeIgnoreCase("SAVE50"))
                    .thenReturn(Optional.of(coupon(50, 200, 100, 0, true, false)));

            Coupon result = couponService.validateForSubtotal("save50", 250);

            assertThat(result.getCode()).isEqualTo("SAVE50");
        }

        @Test
        @DisplayName("rejects an unknown code")
        void rejectsUnknownCode() {
            when(couponRepository.findByCodeIgnoreCase(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.validateForSubtotal("NOPE", 250))
                    .isInstanceOf(CouponException.class)
                    .hasMessageContaining("Invalid coupon code");
        }

        @Test
        @DisplayName("rejects an inactive coupon")
        void rejectsInactive() {
            when(couponRepository.findByCodeIgnoreCase(any()))
                    .thenReturn(Optional.of(coupon(50, 200, 100, 0, false, false)));

            assertThatThrownBy(() -> couponService.validateForSubtotal("SAVE50", 250))
                    .isInstanceOf(CouponException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("rejects an expired coupon")
        void rejectsExpired() {
            when(couponRepository.findByCodeIgnoreCase(any()))
                    .thenReturn(Optional.of(coupon(50, 200, 100, 0, true, true)));

            assertThatThrownBy(() -> couponService.validateForSubtotal("SAVE50", 250))
                    .isInstanceOf(CouponException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("rejects when the usage limit is reached")
        void rejectsUsageLimitReached() {
            when(couponRepository.findByCodeIgnoreCase(any()))
                    .thenReturn(Optional.of(coupon(50, 200, 100, 100, true, false)));

            assertThatThrownBy(() -> couponService.validateForSubtotal("SAVE50", 250))
                    .isInstanceOf(CouponException.class)
                    .hasMessageContaining("usage limit");
        }

        @Test
        @DisplayName("rejects when subtotal is below min spend")
        void rejectsBelowMinSpend() {
            when(couponRepository.findByCodeIgnoreCase(any()))
                    .thenReturn(Optional.of(coupon(50, 200, 100, 0, true, false)));

            assertThatThrownBy(() -> couponService.validateForSubtotal("SAVE50", 150))
                    .isInstanceOf(CouponException.class)
                    .hasMessageContaining("minimum spend");
        }
    }

    @Nested
    @DisplayName("redeem")
    class Redeem {

        @Test
        @DisplayName("increments usedCount")
        void incrementsUsedCount() {
            when(couponRepository.findByCodeIgnoreCase("SAVE50"))
                    .thenReturn(Optional.of(coupon(50, 200, 100, 3, true, false)));
            when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            couponService.redeem("SAVE50");

            ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
            verify(couponRepository).save(captor.capture());
            assertThat(captor.getValue().getUsedCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("is a no-op for a null/blank code")
        void noOpForBlank() {
            couponService.redeem(null);
            couponService.redeem("  ");
            verify(couponRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("rejects a duplicate code")
        void rejectsDuplicateCode() {
            when(couponRepository.existsByCodeIgnoreCase("SAVE50")).thenReturn(true);

            assertThatThrownBy(() -> couponService.create(CouponDTO.builder()
                    .code("save50").amount(50).expiresAt(DateUtil.now().plusDays(5))
                    .usageLimit(100).active(true).build()))
                    .isInstanceOf(CouponException.class)
                    .hasMessageContaining("already exists");

            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("normalizes code to uppercase and starts usedCount at 0")
        void createsWithZeroUsage() {
            when(couponRepository.existsByCodeIgnoreCase(any())).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            CouponDTO result = couponService.create(CouponDTO.builder()
                    .code("save50").amount(50).minSpend(200)
                    .expiresAt(DateUtil.now().plusDays(5)).usageLimit(100).active(true).build());

            assertThat(result.getCode()).isEqualTo("SAVE50");
            assertThat(result.getUsedCount()).isZero();
        }
    }
}
