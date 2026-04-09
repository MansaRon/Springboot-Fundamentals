package co.za.ecommerce.api.impl;

import co.za.ecommerce.api.CheckoutAPI;
import co.za.ecommerce.dto.api.ApiResource;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/checkout")
public class CheckoutAPIImpl extends API implements CheckoutAPI {

    @Secured({"ROLE_USER"})
    @PostMapping("/{userId}/initiate-checkout")
    public ResponseEntity<CheckoutDTOApiResource> initiateCheckout(@PathVariable ObjectId userId) {
        log.info("ResponseEntity<CheckoutDTOApiResource> initiateCheckout(@PathVariable ObjectId userId)");
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.createCheckoutFromCart(userId))
                        .message("Checkout initiated.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/cart/{cartId}")
    public ResponseEntity<CheckoutDTOApiResource> getCheckoutByCart(@PathVariable ObjectId cartId) {
        log.info("ResponseEntity<CheckoutDTOApiResource> getCheckoutByCart(@PathVariable ObjectId cartId)");
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(now())
                        .data(checkoutService.getCheckoutByCartId(cartId))
                        .message("Checkout retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Secured({"ROLE_USER"})
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<CheckoutDTOApiResource> getCheckoutsByStatus(@PathVariable String status) {
        log.info("ResponseEntity<CheckoutDTOApiResource> getCheckoutsByStatus(@PathVariable String status)");
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

    @Secured({"ROLE_USER"})
    @PatchMapping("/{userId}")
    public ResponseEntity<CheckoutDTOApiResource> updateCheckout(@PathVariable ObjectId userId, @Valid @RequestBody CheckoutDTO checkoutDTO) {
        log.info("ResponseEntity<CheckoutDTOApiResource> updateCheckout(@PathVariable ObjectId userId, @Valid @RequestBody CheckoutDTO checkoutDTO)");
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


    @Override
    @Secured({"ROLE_USER"})
    @PostMapping("/{checkoutId}/confirm")
    public ResponseEntity<OrderDTOApiResource> confirmCheckout(@PathVariable ObjectId checkoutId) {
        log.info("ResponseEntity<OrderDTOApiResource> confirmCheckout(@PathVariable ObjectId checkoutId)");
        return ResponseEntity.ok(
                OrderDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(checkoutService.confirmCheckout(checkoutId))
                        .message("Order placed successfully!")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @Secured({"ROLE_USER"})
    @GetMapping("/user/{userId}")
    public ResponseEntity<CheckoutDTOApiResource> getUserCheckout(@PathVariable ObjectId userId) {
        log.info("ResponseEntity<CheckoutDTOApiResource> getUserCheckout(@PathVariable ObjectId userId)");
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(checkoutService.getCheckoutByUserId(userId))
                        .message("Active checkout retrieved.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @DeleteMapping("/{checkoutId}")
    public ResponseEntity<ApiResource> cancelCheckout(@PathVariable ObjectId checkoutId) {
        log.info("ResponseEntity<ApiResource> cancelCheckout(@PathVariable ObjectId checkoutId)");
        checkoutService.cancelCheckout(checkoutId);
        return ResponseEntity.ok(
                ApiResource
                        .builder()
                        .timestamp(Instant.now())
                        .message("Checkout cancelled successfully.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<CheckoutDTOApiResource> deleteUserCheckouts(@PathVariable ObjectId userId) {
        log.info("ResponseEntity<CheckoutDTOApiResource> deleteUserCheckouts(@PathVariable ObjectId userId)");
        CheckoutDTO deleted = checkoutService.deleteCheckoutByUserId(userId);
        return ResponseEntity.ok(
                CheckoutDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(deleted)
                        .message("User checkouts deleted.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
