package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.CartAPI;
import co.za.ecommerce.dto.api.CartDTOApiResource;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cart")
public class CartAPIImpl extends API implements CartAPI {

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @Override
    @PermitAll
    @PostMapping("/{userId}/add-item/{productId}")
    public ResponseEntity<CartDTOApiResource> addProductToCart(
            @PathVariable ObjectId userId,
            @PathVariable ObjectId productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(
                CartDTOApiResource.builder()
                        .timestamp(now())
                        .data(cartService.addProductToCart(userId, productId, quantity))
                        .message("Product Added To Cart")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @GetMapping("/{userId}")
    public ResponseEntity<CartDTOApiResource> getUserCartWithItems(
            @PathVariable ObjectId userId) {
        return ResponseEntity.ok(
                CartDTOApiResource.builder()
                        .timestamp(now())
                        .data(cartService.getUserCartWithItems(userId))
                        .message("Cart with user ID " + userId + " retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PatchMapping("/{userId}/update-item/{productId}")
    public ResponseEntity<CartDTOApiResource> updateProductInCart(
            @PathVariable ObjectId userId,
            @PathVariable ObjectId productId,
            @RequestParam int newQuantity) {
        return ResponseEntity.ok(
                CartDTOApiResource.builder()
                        .timestamp(now())
                        .data(cartService.updateProductInCart(userId, productId, newQuantity))
                        .message("Cart with user ID " + userId + " updated.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @DeleteMapping("/{userId}/delete-item/{productId}")
    public ResponseEntity<CartDTOApiResource> deleteProductInCart(
            @PathVariable ObjectId userId,
            @PathVariable ObjectId productId) {
        return ResponseEntity.ok(
                CartDTOApiResource.builder()
                        .timestamp(now())
                        .data(cartService.deleteProductFromCart(userId, productId))
                        .message("Cart with user ID " + userId + " deleted.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
