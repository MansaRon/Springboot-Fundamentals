package co.za.ecommerce.api.impl;

import co.za.ecommerce.dto.api.CheckoutDTOApiResource;
import co.za.ecommerce.dto.api.OrderDTOApiResource;
import co.za.ecommerce.dto.checkout.CheckoutDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/checkout")
public class CheckoutAPIImpl extends API {

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PostMapping("/{userId}/initiate-checkout")
    public ResponseEntity<CheckoutDTOApiResource> initiateCheckout(
            @PathVariable ObjectId userId) {
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.initiateCheckout(userId))
                        .message("Checkout initiated.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PostMapping("/{userId}/current")
    public ResponseEntity<CheckoutDTOApiResource> getCheckoutByUserId(
            @PathVariable ObjectId userId) {
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.getCheckoutByUserId(userId))
                        .message("Checkout by user ID.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PostMapping("/{cartId}/retrieve-checkout")
    public ResponseEntity<CheckoutDTOApiResource> getCheckoutByCartId(
            @PathVariable ObjectId cartId) {
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.getCheckoutByCartId(cartId))
                        .message("Checkout by cart ID.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @GetMapping("/{status}/checkout-list")
    public ResponseEntity<CheckoutDTOApiResource> getCheckoutsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(now())
                        .dataList(checkoutService.getCheckoutsByStatus(status))
                        .message("Retrieve checkout by status.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PostMapping("/{userId}/update-checkout")
    public ResponseEntity<CheckoutDTOApiResource> updateCheckout(
            @PathVariable ObjectId userId,
            @Valid @RequestBody CheckoutDTO checkoutDTO) {
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.updateCheckout(userId, checkoutDTO))
                        .dataList(new ArrayList<>())
                        .message("Checkout items updated.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PostMapping("/{cartId}/confirm-checkout")
    public ResponseEntity<OrderDTOApiResource> confirmCheckout(
            @PathVariable ObjectId cartId) {
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.confirmCheckout(cartId))
                        .message("Checkout confirmed and order created.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @DeleteMapping("/{userId}")
    public ResponseEntity deleteCheckoutByUserId(@PathVariable ObjectId userId) {
        checkoutService.deleteCheckoutByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
