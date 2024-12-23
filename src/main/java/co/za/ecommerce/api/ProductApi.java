package co.za.ecommerce.api;

import co.za.ecommerce.config.AppConstants;
import co.za.ecommerce.dto.api.ProductDTOAllApiResource;
import co.za.ecommerce.dto.api.ProductDTOApiResource;
import co.za.ecommerce.dto.api.ProductDTOListApiResource;
import co.za.ecommerce.dto.product.ProductDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.time.Instant.now;

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
                        .timestamp(now())
                        .data(productService.addProduct(productDTO))
                        .message("Product Added")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    @PermitAll
    @GetMapping
    public ResponseEntity<ProductDTOAllApiResource> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        return ResponseEntity.ok(
                ProductDTOAllApiResource.builder()
                        .timestamp(now())
                        .data(productService.getAllPosts(pageNumber, pageSize, sortBy, sortOrder))
                        .message("All Products retrieved")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTOApiResource> getProduct(@PathVariable String id) {
        log.trace("public ResponseEntity<ProductDTOApiResource> getProduct(@PathVariable String id)");
        return ResponseEntity.ok(
                ProductDTOApiResource.builder()
                        .timestamp(now())
                        .data(productService.getProduct(id))
                        .message("Product Retrieved")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PermitAll
    @GetMapping("/get/{category}")
    public ResponseEntity<ProductDTOAllApiResource> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        log.trace("public ResponseEntity<ProductDTOAllApiResource> getProductsByCategory(@PathVariable String category)");
        return ResponseEntity.ok(
                ProductDTOAllApiResource.builder()
                        .timestamp(now())
                        .data(productService.getProductByCategory(category, pageNumber, pageSize, sortBy, sortOrder))
                        .message("List of products with keyword " + category + ".")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PermitAll
    @PostMapping("/list")
    public ResponseEntity<ProductDTOListApiResource> createListProduct(
            @Valid @RequestBody List<ProductDTO> productDTO) {
        log.trace("public ResponseEntity<ProductDTOListApiResource> createListProduct(@Valid @RequestBody ProductDTO productDTO)");
        return ResponseEntity.ok(
                ProductDTOListApiResource.builder()
                        .timestamp(now())
                        .data(productService.addMultipleProducts(productDTO))
                        .message("Multiple Products Added")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }
}
