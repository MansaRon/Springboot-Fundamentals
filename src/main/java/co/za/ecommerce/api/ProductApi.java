package co.za.ecommerce.api;

import co.za.ecommerce.dto.api.ProductDTOApiResource;
import co.za.ecommerce.dto.product.ProductDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products")
public class ProductApi extends API {

    // To change soon to only admin, permitAll for testing only
    @PermitAll
    @PostMapping("/product")
    public ResponseEntity<ProductDTOApiResource> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        log.trace("public ResponseEntity<ProductDTOApiResource> product(@Valid @RequestBody ProductDTO productDTO)");
        return ResponseEntity.ok(
                ProductDTOApiResource.builder()
                        .timestamp(Instant.now())
                        .data(productService.addProduct(productDTO))
                        .message("Product Added")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }
}
