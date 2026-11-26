package co.za.ecommerce.business;

import co.za.ecommerce.dto.coupon.CouponDTO;
import co.za.ecommerce.dto.coupon.CouponValidationDTO;
import co.za.ecommerce.model.Coupon;
import org.bson.types.ObjectId;

import java.util.List;

public interface CouponService {

    // Admin management
    CouponDTO create(CouponDTO dto);
    CouponDTO update(ObjectId id, CouponDTO dto);
    void delete(ObjectId id);
    List<CouponDTO> getAll();
    CouponDTO getById(ObjectId id);

    /**
     * Validates that the code is usable for an order of the given subtotal (active, not
     * expired, under its usage cap, meets min spend) and returns the matching coupon.
     * Throws {@link co.za.ecommerce.exception.CouponException} otherwise.
     */
    Coupon validateForSubtotal(String code, double subtotal);

    /**
     * Non-throwing preview of {@link #validateForSubtotal}: returns whether the code is usable
     * for the given subtotal and the discount it would apply, instead of throwing.
     */
    CouponValidationDTO previewForSubtotal(String code, double subtotal);

    /** Records a redemption (increments usedCount). No-op if the code is null/blank. */
    void redeem(String code);
}
