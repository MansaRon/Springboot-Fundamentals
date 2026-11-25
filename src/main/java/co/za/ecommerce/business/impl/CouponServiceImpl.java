package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.CouponService;
import co.za.ecommerce.dto.coupon.CouponDTO;
import co.za.ecommerce.exception.CouponException;
import co.za.ecommerce.mapper.CouponMapper;
import co.za.ecommerce.model.Coupon;
import co.za.ecommerce.repository.CouponRepository;
import co.za.ecommerce.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public CouponDTO create(CouponDTO dto) {
        String code = normalize(dto.getCode());
        log.info("Creating coupon {}", code);

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new CouponException(
                    HttpStatus.CONFLICT.toString(),
                    "A coupon with code '" + code + "' already exists.",
                    HttpStatus.CONFLICT.value());
        }

        Coupon coupon = Coupon.builder()
                .createdAt(DateUtil.now())
                .updatedAt(DateUtil.now())
                .code(code)
                .amount(dto.getAmount())
                .minSpend(dto.getMinSpend())
                .expiresAt(dto.getExpiresAt())
                .usageLimit(dto.getUsageLimit())
                .usedCount(0)
                .active(dto.isActive())
                .build();
        return CouponMapper.toDTO(couponRepository.save(coupon));
    }

    @Override
    public CouponDTO update(ObjectId id, CouponDTO dto) {
        log.info("Updating coupon {}", id);
        Coupon coupon = findById(id);

        String newCode = normalize(dto.getCode());
        // Guard against renaming onto an existing code.
        if (!coupon.getCode().equalsIgnoreCase(newCode) && couponRepository.existsByCodeIgnoreCase(newCode)) {
            throw new CouponException(
                    HttpStatus.CONFLICT.toString(),
                    "A coupon with code '" + newCode + "' already exists.",
                    HttpStatus.CONFLICT.value());
        }

        coupon.setCode(newCode);
        coupon.setAmount(dto.getAmount());
        coupon.setMinSpend(dto.getMinSpend());
        coupon.setExpiresAt(dto.getExpiresAt());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setActive(dto.isActive());
        coupon.setUpdatedAt(DateUtil.now());
        // usedCount is server-managed and intentionally not overwritten from the request.

        return CouponMapper.toDTO(couponRepository.save(coupon));
    }

    @Override
    public void delete(ObjectId id) {
        log.info("Deleting coupon {}", id);
        if (!couponRepository.existsById(id)) {
            throw new CouponException(
                    HttpStatus.NOT_FOUND.toString(),
                    "Coupon not found.",
                    HttpStatus.NOT_FOUND.value());
        }
        couponRepository.deleteById(id);
    }

    @Override
    public List<CouponDTO> getAll() {
        return CouponMapper.toDTOList(couponRepository.findAll());
    }

    @Override
    public CouponDTO getById(ObjectId id) {
        return CouponMapper.toDTO(findById(id));
    }

    @Override
    public Coupon validateForSubtotal(String code, double subtotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(normalize(code))
                .orElseThrow(() -> badRequest("Invalid coupon code."));

        if (!coupon.isActive()) {
            throw badRequest("This coupon is not active.");
        }
        if (coupon.getExpiresAt() != null && DateUtil.now().isAfter(coupon.getExpiresAt())) {
            throw badRequest("This coupon has expired.");
        }
        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw badRequest("This coupon has reached its usage limit.");
        }
        if (subtotal < coupon.getMinSpend()) {
            throw badRequest(String.format("A minimum spend of %.2f is required to use this coupon.",
                    coupon.getMinSpend()));
        }
        return coupon;
    }

    @Override
    public void redeem(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        couponRepository.findByCodeIgnoreCase(normalize(code)).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            coupon.setUpdatedAt(DateUtil.now());
            couponRepository.save(coupon);
            log.info("Coupon {} redeemed — usedCount now {}", coupon.getCode(), coupon.getUsedCount());
        });
    }

    private Coupon findById(ObjectId id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponException(
                        HttpStatus.NOT_FOUND.toString(),
                        "Coupon not found.",
                        HttpStatus.NOT_FOUND.value()));
    }

    private String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private CouponException badRequest(String message) {
        return new CouponException(HttpStatus.BAD_REQUEST.toString(), message, HttpStatus.BAD_REQUEST.value());
    }
}
