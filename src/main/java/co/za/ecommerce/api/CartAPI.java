package co.za.ecommerce.api;

import co.za.ecommerce.dto.api.CartDTOApiResource;
import co.za.ecommerce.dto.api.ProductDTOApiResource;
import co.za.ecommerce.dto.product.ProductDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cart")
public class CartAPI extends API {

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PostMapping
    public ResponseEntity<CartDTOApiResource> addProductToCart(
            @RequestParam ObjectId userId,
            @RequestParam ObjectId productId,
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
}
