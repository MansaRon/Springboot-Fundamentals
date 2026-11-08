package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.AdminReviewAPI;
import co.za.ecommerce.dto.api.RatingDTOApiResource;
import co.za.ecommerce.validation.ValidObjectId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.time.Instant.now;

/**
 * ADMIN only — moderation of product reviews. Non-admin callers get 403.
 */
@Slf4j
@RestController
@RequestMapping("api/v1/admin/products/{productId}/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewAPIImpl extends API implements AdminReviewAPI {

    @Override
    @GetMapping
    public ResponseEntity<RatingDTOApiResource> getAllReviews(@PathVariable @ValidObjectId String productId) {
        log.info("ResponseEntity<RatingDTOApiResource> getAllReviews(@PathVariable String productId)");
        return ResponseEntity.ok(
                RatingDTOApiResource.builder()
                        .timestamp(now())
                        .reviews(productService.getAllProductReviews(productId))
                        .message("All reviews retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @PatchMapping("/{userId}/hide")
    public ResponseEntity<RatingDTOApiResource> hideReview(@PathVariable @ValidObjectId String productId,
                                                           @PathVariable @ValidObjectId String userId) {
        log.info("ResponseEntity<RatingDTOApiResource> hideReview(@PathVariable String productId, @PathVariable String userId)");
        return ResponseEntity.ok(
                RatingDTOApiResource.builder()
                        .timestamp(now())
                        .data(productService.setReviewVisibility(productId, userId, true))
                        .message("Review hidden.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @PatchMapping("/{userId}/approve")
    public ResponseEntity<RatingDTOApiResource> approveReview(@PathVariable @ValidObjectId String productId,
                                                              @PathVariable @ValidObjectId String userId) {
        log.info("ResponseEntity<RatingDTOApiResource> approveReview(@PathVariable String productId, @PathVariable String userId)");
        return ResponseEntity.ok(
                RatingDTOApiResource.builder()
                        .timestamp(now())
                        .data(productService.setReviewVisibility(productId, userId, false))
                        .message("Review approved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @DeleteMapping("/{userId}")
    public ResponseEntity<RatingDTOApiResource> removeReview(@PathVariable @ValidObjectId String productId,
                                                             @PathVariable @ValidObjectId String userId) {
        log.info("ResponseEntity<RatingDTOApiResource> removeReview(@PathVariable String productId, @PathVariable String userId)");
        productService.deleteRating(productId, userId);
        return ResponseEntity.ok(
                RatingDTOApiResource.builder()
                        .timestamp(now())
                        .message("Review removed.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
