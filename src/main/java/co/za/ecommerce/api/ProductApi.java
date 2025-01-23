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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static java.time.Instant.now;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products")
public class ProductApi extends API {

    // To change soon to only admin, permitAll for testing only
    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTOApiResource> createProduct(
            @RequestPart("product") @Valid String productJson,
            @RequestPart("images") @Valid List<MultipartFile> imageFiles) throws IOException {
        ProductDTO productDTO = jsonMapper.readValue(productJson, ProductDTO.class);
        return ResponseEntity.ok(
                ProductDTOApiResource.builder()
                        .timestamp(now())
                        .data(productService.addProduct(productDTO, imageFiles))
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
    public ResponseEntity<ProductDTOApiResource> getProduct(
            @PathVariable String id) {
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
    @GetMapping("/category/{category}")
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
    @GetMapping("/title/{title}")
    public ResponseEntity<ProductDTOAllApiResource> getProductsByTitle(
            @PathVariable String title,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        log.trace("public ResponseEntity<ProductDTOAllApiResource> getProductsByTitle(@PathVariable String title)");
        return ResponseEntity.ok(
                ProductDTOAllApiResource.builder()
                        .timestamp(now())
                        .data(productService.getProductByTitle(title, pageNumber, pageSize, sortBy, sortOrder))
                        .message("List of products with keyword " + title + ".")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PermitAll
    @PostMapping("/list")
    public ResponseEntity<ProductDTOListApiResource> createListProduct(
            @Valid @RequestBody List<ProductDTO> productDTO,
            @RequestPart("images") @Valid List<MultipartFile> imageFiles) throws IOException {
        log.trace("public ResponseEntity<ProductDTOListApiResource> createListProduct(@Valid @RequestBody ProductDTO productDTO)");
        return ResponseEntity.ok(
                ProductDTOListApiResource.builder()
                        .timestamp(now())
                        .data(productService.addMultipleProducts(productDTO, imageFiles))
                        .message("Multiple Products Added")
                        .status(String.valueOf(HttpStatus.CREATED))
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    // @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    // @Secured({"USER"})
    @PermitAll
    @PatchMapping("/product/{productId}")
    public ResponseEntity<ProductDTOApiResource> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody ProductDTO productDTO,
            @RequestPart("images") List<MultipartFile> imageFiles) throws IOException {
        log.trace("public ResponseEntity<ProductDTOApiResource> updateProduct(@Valid @RequestBody ProductDTO productDTO)");
        return ResponseEntity.ok(
                ProductDTOApiResource.builder()
                        .timestamp(now())
                        .data(productService.updateProduct(productId, productDTO, imageFiles))
                        .message("Product Updated.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // @PreAuthorize("hasRole('ADMIN')")
    // @Secured({"ADMIN"})
    @PermitAll
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ProductDTOApiResource> deleteProduct(
            @PathVariable String productId) {
        log.trace("public ResponseEntity<ProductDTOApiResource> deleteProduct(@PathVariable String productId)");
        return ResponseEntity.ok(
                ProductDTOApiResource.builder()
                        .timestamp(now())
                        .delete(productService.deleteProduct(productId))
                        .message("Product Deleted.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // @PreAuthorize("hasRole('ADMIN')")
    // @Secured({"ADMIN"})
    @PermitAll
    @DeleteMapping("/product")
    public ResponseEntity<ProductDTOApiResource> deleteAllProduct() {
        log.trace("public ResponseEntity<ProductDTOApiResource> deleteAllProduct()");
        return ResponseEntity.ok(
                ProductDTOApiResource.builder()
                        .timestamp(now())
                        .delete(productService.deleteAllProducts())
                        .message("All products deleted.")
                        .status(String.valueOf(HttpStatus.OK))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
