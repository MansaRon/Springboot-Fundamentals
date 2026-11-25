package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.AdminCouponAPI;
import co.za.ecommerce.dto.api.ApiResource;
import co.za.ecommerce.dto.api.CouponApiResource;
import co.za.ecommerce.dto.api.CouponListApiResource;
import co.za.ecommerce.dto.coupon.CouponDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static java.time.Instant.now;

/**
 * ADMIN only — CRUD for order-level discount coupons. Non-admin callers get 403.
 */
@Slf4j
@RestController
@RequestMapping("api/v1/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCouponAPIImpl extends API implements AdminCouponAPI {

    @Override
    @PostMapping
    public ResponseEntity<CouponApiResource> create(@Valid @RequestBody CouponDTO request) {
        log.info("ResponseEntity<CouponApiResource> create(@Valid @RequestBody CouponDTO request)");
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CouponApiResource.builder()
                        .timestamp(now())
                        .data(couponService.create(request))
                        .message("Coupon created.")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @Override
    @GetMapping
    public ResponseEntity<CouponListApiResource> getAll() {
        log.info("ResponseEntity<CouponListApiResource> getAll()");
        return ResponseEntity.ok(
                CouponListApiResource.builder()
                        .timestamp(now())
                        .data(couponService.getAll())
                        .message("Coupons retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CouponApiResource> getById(@PathVariable ObjectId id) {
        log.info("ResponseEntity<CouponApiResource> getById(@PathVariable ObjectId id)");
        return ResponseEntity.ok(
                CouponApiResource.builder()
                        .timestamp(now())
                        .data(couponService.getById(id))
                        .message("Coupon retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CouponApiResource> update(@PathVariable ObjectId id, @Valid @RequestBody CouponDTO request) {
        log.info("ResponseEntity<CouponApiResource> update(@PathVariable ObjectId id, @Valid @RequestBody CouponDTO request)");
        return ResponseEntity.ok(
                CouponApiResource.builder()
                        .timestamp(now())
                        .data(couponService.update(id, request))
                        .message("Coupon updated.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResource> delete(@PathVariable ObjectId id) {
        log.info("ResponseEntity<ApiResource> delete(@PathVariable ObjectId id)");
        couponService.delete(id);
        return ResponseEntity.ok(
                ApiResource.builder()
                        .timestamp(now())
                        .message("Coupon deleted.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
