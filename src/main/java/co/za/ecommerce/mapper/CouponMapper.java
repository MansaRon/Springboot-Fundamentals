package co.za.ecommerce.mapper;

import co.za.ecommerce.dto.coupon.CouponDTO;
import co.za.ecommerce.model.Coupon;

import java.util.List;
import java.util.stream.Collectors;

public class CouponMapper {

    private CouponMapper() {
    }

    public static CouponDTO toDTO(Coupon coupon) {
        if (coupon == null) return null;
        return CouponDTO.builder()
                .id(coupon.getId() != null ? coupon.getId().toHexString() : null)
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .code(coupon.getCode())
                .amount(coupon.getAmount())
                .minSpend(coupon.getMinSpend())
                .expiresAt(coupon.getExpiresAt())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .active(coupon.isActive())
                .build();
    }

    public static List<CouponDTO> toDTOList(List<Coupon> coupons) {
        if (coupons == null) return List.of();
        return coupons.stream().map(CouponMapper::toDTO).collect(Collectors.toList());
    }
}
