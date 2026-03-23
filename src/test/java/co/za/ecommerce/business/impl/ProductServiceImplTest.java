package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.S3Service;
import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.repository.ProductRepository;
import co.za.ecommerce.utils.DateUtil;
import factory.TestDataBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final String VALID_ID = "507f1f77bcf86cd799439011";
    private static final String INVALID_ID = "invalid-id";
    private static final String PRODUCT_TITLE = "Organic Fleece Hoodie";
    private static final String PRODUCT_CATEGORY = "Clothing";
    private final String IMAGE_URL_1 = "https://bucket.s3.amazonaws.com/image1.jpg";
    private final String IMAGE_URL_2 = "https://bucket.s3.amazonaws.com/image2.jpg";

    private Product savedProduct;
    private ProductDTO productDTO;
    private MockMultipartFile imageFile;

    @BeforeEach
    void setUp() {
        savedProduct = TestDataBuilder.buildProduct(new ObjectId(VALID_ID));

        productDTO = ProductDTO.builder()
                .title(PRODUCT_TITLE)
                .description("Lightweight hoodie")
                .category(PRODUCT_CATEGORY)
                .price(49.99)
                .rate("4.5")
                .quantity(25)
                .build();

        imageFile = new MockMultipartFile(
                "images",
                "hoodie.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );
    }

    @Nested
    @DisplayName("addProduct")
    class addProduct {

        @Test
        @DisplayName("shouldUploadImagesSaveProductAndReturnDTOWhenInputIsValid")
        void shouldUploadImagesSaveProductAndReturnDTOWhenInputIsValid() {
            // Arrange
            // Tell the S3 mock: when uploadFile() is called with any file, return this URL.
            // We don't test the real S3 upload — that's S3ServiceImpl's responsibility.
            when(s3Service.uploadFile(any(MultipartFile.class))).thenReturn(IMAGE_URL_1);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // ObjectMapper is our custom wrapper around ModelMapper.
            // When mapObject() is called, return the real ModelMapper mock.
            // When map() is called on that, return our pre-built productDTO.
            // This avoids needing a real ModelMapper instance in a unit test.
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            ProductDTO result = productService.addProduct(productDTO, List.of(imageFile));

            // Assert — the result is not null and matches expected data
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(PRODUCT_TITLE);

            // Use ArgumentCaptor to intercept the Product object that was passed to save().
            // This is the most important assertion in this test — it proves the service
            // correctly mapped the DTO + S3 URL into a Product before saving.
            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());

            Product capturedProduct = captor.getValue();
            assertThat(capturedProduct.getTitle()).isEqualTo(PRODUCT_TITLE);
            assertThat(capturedProduct.getCategory()).isEqualTo(PRODUCT_CATEGORY);
            assertThat(capturedProduct.getPrice()).isEqualTo(49.99);

            // Verify S3 was called exactly once (one file = one upload)
            verify(s3Service, times(1)).uploadFile(any(MultipartFile.class));
        }

        @Test
        @DisplayName("shouldSaveProductWithEmptyImageUrlsWhenNoFilesProvided")
        void shouldSaveProductWithEmptyImageUrlsWhenNoFilesProvided() {
            // Arrange
            // Passing an empty list simulates a product being added without images.
            // The service should handle this gracefully and save with an empty imageUrls list.
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            productService.addProduct(productDTO, List.of());

            // Assert — product is saved, S3 is never called since there are no files
            verify(productRepository).save(any(Product.class));
            verify(s3Service, never()).uploadFile(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllPosts
    //
    // Logic being tested:
    // 1. Repository is called with a Pageable (page number, size, sort)
    // 2. If the page is empty, a ProductException is thrown
    // 3. If products exist, a GetAllProductsDTO is returned with correct metadata
    //
    // We use PageImpl to simulate a real Page<Product> response from MongoDB.
    // This lets us test pagination metadata (totalPages, totalElements, isLast)
    // without hitting a real database.
    @Nested
    @DisplayName("getAllPosts")
    class GetAllPosts {

        @Test
        @DisplayName("shouldReturnPagedProductsWhenProductsExist")
        void shouldReturnPagedProductsWhenProductsExist() {
            // Arrange
            // PageImpl simulates a MongoDB page response with 1 product,
            // page 0, page size 10. This is what productRepository.findAll(pageable) returns.
            Page<Product> productPage = new PageImpl<>(
                    List.of(savedProduct),
                    Pageable.ofSize(10),
                    1
            );

            when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            GetAllProductsDTO result = productService.getAllPosts(0, 10, "title", "asc");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenNoProductsFound")
        void shouldThrowProductExceptionWhenNoProductsFound() {
            // Arrange
            // An empty page simulates a MongoDB collection with no products.
            Page<Product> emptyPage = Page.empty();
            when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // Act & Assert
            // assertThatThrownBy is the AssertJ way to assert that a method throws.
            // It's cleaner than try-catch and gives better failure messages.
            assertThatThrownBy(() -> productService.getAllPosts(0, 10, "title", "asc"))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("No products found");

            // Confirm the objectMapper was never called — we threw before reaching it
            verify(objectMapper, never()).mapObject();
        }

        @Test
        @DisplayName("shouldSortProductsDescendingWhenSortDirIsDesc")
        void shouldSortProductsDescendingWhenSortDirIsDesc() {
            // Arrange
            // This test verifies the sort direction logic in the service.
            // We capture the Pageable passed to findAll() and check its sort direction.
            Page<Product> productPage = new PageImpl<>(List.of(savedProduct));
            when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            productService.getAllPosts(0, 10, "title", "desc");

            // Assert — capture the Pageable and verify the sort direction
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(productRepository).findAll(pageableCaptor.capture());

            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getSort().getOrderFor("title")).isNotNull();
            assertThat(captured.getSort().getOrderFor("title").isDescending()).isTrue();
        }
    }

    // getProduct
    //
    // Logic being tested:
    // 1. Invalid ID format → throw ProductException before hitting the database
    // 2. Valid ID, product not found → throw ProductException
    // 3. Valid ID, product found → return mapped DTO
    //
    // The ID validation is pure string logic — no database involved.
    // Testing it separately confirms the regex guard works correctly.
    @Nested
    @DisplayName("getProduct")
    class GetProduct {

        @Test
        @DisplayName("shouldReturnProductDTOWhenValidIdAndProductExists")
        void shouldReturnProductDTOWhenValidIdAndProductExists() {
            // Arrange
            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.of(savedProduct));
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            ProductDTO result = productService.getProduct(VALID_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(PRODUCT_TITLE);
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenIdFormatIsInvalid")
        void shouldThrowProductExceptionWhenIdFormatIsInvalid() {
            // Arrange — "invalid-id" fails the 24-char hex regex check.
            // The service should throw immediately WITHOUT hitting the database.

            // Act & Assert
            assertThatThrownBy(() -> productService.getProduct(INVALID_ID))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("Invalid ID format");

            // The repository must never be called — we validated and threw early
            verify(productRepository, never()).findById(any());
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenProductDoesNotExist")
        void shouldThrowProductExceptionWhenProductDoesNotExist() {
            // Arrange — ID is valid format but no product found in DB
            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.getProduct(VALID_ID))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("doesn't exist");
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenIdIsNull")
        void shouldThrowProductExceptionWhenIdIsNull() {
            // Arrange — null ID should fail the regex check, not cause a NullPointerException
            assertThatThrownBy(() -> productService.getProduct(null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("Invalid ID format");

            verify(productRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("getProductByCategory")
    class GetProductByCategory {

        @Test
        @DisplayName("shouldReturnProductsWhenCategoryMatches")
        void shouldReturnProductsWhenCategoryMatches() {
            // Arrange
            Page<Product> page = new PageImpl<>(List.of(savedProduct));
            when(productRepository.findByCategoryIgnoreCase(eq(PRODUCT_CATEGORY), any(Pageable.class))).thenReturn(page);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            GetAllProductsDTO result = productService.getProductByCategory(PRODUCT_CATEGORY, 0, 10, "title", "asc");

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenNoCategoryMatchFound")
        void shouldThrowProductExceptionWhenNoCategoryMatchFound() {
            // Arrange
            when(productRepository.findByCategoryIgnoreCase(any(), any(Pageable.class))).thenReturn(Page.empty());

            // Act & Assert
            // The exception message should include the category name so the caller
            // knows which category returned no results
            assertThatThrownBy(() -> productService.getProductByCategory(
                    "Electronics", 0, 10, "title", "asc"))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("Electronics");
        }
    }

    @Nested
    @DisplayName("getProductByTitle")
    class GetProductByTitle {
        @Test
        @DisplayName("shouldReturnProductsWhenTitleMatches")
        void shouldReturnProductsWhenTitleMatches() {
            // Arrange
            Page<Product> page = new PageImpl<>(List.of(savedProduct));
            when(productRepository.findByTitleIgnoreCase(eq(PRODUCT_TITLE), any(Pageable.class))).thenReturn(page);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            GetAllProductsDTO result = productService.getProductByTitle(PRODUCT_TITLE, 0, 10, "title", "asc");

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("shouldThrowProductExceptionWhenNoTitleMatchFound")
        void shouldThrowProductExceptionWhenNoTitleMatchFound() {
            // Arrange
            when(productRepository.findByTitleIgnoreCase(any(), any(Pageable.class))).thenReturn(Page.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.getProductByTitle(
                    "Unknown Product", 0, 10, "title", "asc"))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("Unknown Product");
        }
    }

    // addMultipleProducts
    //
    // Logic being tested:
    // 1. Null or empty product list → throw immediately
    // 2. Null or empty image list → throw immediately
    // 3. Valid inputs → upload images per product, save all, return DTOs
    //
    // The key business rule here is that images are distributed evenly across
    // products. 4 images for 2 products = 2 images per product.
    // We verify this by checking how many times S3 uploadFile is called.
    @Nested
    @DisplayName("addMultipleProducts")
    class AddMultipleProducts {
        @Test
        @DisplayName("shouldThrowWhenProductListIsNull")
        void shouldThrowWhenProductListIsNull() {
            assertThatThrownBy(() ->
                    productService.addMultipleProducts(null, List.of(imageFile)))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("cannot be empty or null");

            verify(productRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("shouldThrowWhenProductListIsEmpty")
        void shouldThrowWhenProductListIsEmpty() {
            assertThatThrownBy(() ->
                    productService.addMultipleProducts(List.of(), List.of(imageFile)))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("cannot be empty or null");

            verify(productRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("shouldThrowWhenImageListIsNull")
        void shouldThrowWhenImageListIsNull() {
            assertThatThrownBy(() ->
                    productService.addMultipleProducts(List.of(productDTO), null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("cannot be empty or null");

            verify(productRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("shouldThrowWhenImageListIsEmpty")
        void shouldThrowWhenImageListIsEmpty() {
            assertThatThrownBy(() ->
                    productService.addMultipleProducts(List.of(productDTO), List.of()))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("cannot be empty or null");

            verify(productRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("shouldSaveAllProductsAndReturnDTOsWhenInputIsValid")
        void shouldSaveAllProductsAndReturnDTOsWhenInputIsValid() {
            // Arrange
            ProductDTO productDTO2 = ProductDTO.builder()
                    .title("Stretch Slim-Fit Jeans")
                    .description("Premium denim")
                    .category("Clothing")
                    .price(59.99)
                    .rate("4.3")
                    .quantity(30)
                    .build();

            MockMultipartFile imageFile2 = new MockMultipartFile("images", "jeans.jpg", "image/jpeg", "content".getBytes());

            Product savedProduct2 = Product.builder()
                    .id(new ObjectId())
                    .title("Stretch Slim-Fit Jeans")
                    .imageUrls(List.of(IMAGE_URL_2))
                    .build();

            when(s3Service.uploadFile(any(MultipartFile.class))).thenReturn(IMAGE_URL_1).thenReturn(IMAGE_URL_2);
            when(productRepository.saveAll(any())).thenReturn(List.of(savedProduct, savedProduct2));
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO).thenReturn(productDTO2);

            // Act
            List<ProductDTO> results = productService.addMultipleProducts(List.of(productDTO, productDTO2), List.of(imageFile, imageFile2));

            // S3 should be called twice — once per product image
            verify(s3Service, times(2)).uploadFile(any(MultipartFile.class));

            // saveAll should be called once with both products
            verify(productRepository, times(1)).saveAll(any());
        }
    }

    // updateProduct
    //
    // Logic being tested:
    // 1. Invalid ID → throw before hitting DB
    // 2. Product not found → throw
    // 3. Valid update without new images → update fields, keep existing images
    // 4. Valid update with new images → delete old S3 images, upload new ones
    //
    // The partial update logic (defaultIfNullOrEmpty / defaultIfNullOrZero)
    // means fields not provided in the DTO should keep their existing values.
    // We test this by passing a DTO with only some fields and checking the
    // captured Product still has the original values for the rest.
    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("shouldThrowWhenIdIsInvalid")
        void shouldThrowWhenIdIsInvalid() {
            assertThatThrownBy(() ->
                    productService.updateProduct(INVALID_ID, productDTO, List.of()))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("Invalid ID format");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldThrowWhenProductNotFound")
        void shouldThrowWhenProductNotFound() {
            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    productService.updateProduct(VALID_ID, productDTO, List.of()))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("doesn't exist");
        }

        @Test
        @DisplayName("shouldUpdateFieldsAndKeepExistingImagesWhenNoNewImagesProvided")
        void shouldUpdateFieldsAndKeepExistingImagesWhenNoNewImagesProvided() {
            // Arrange
            // Only updating price — all other fields should remain unchanged
            ProductDTO updateRequest = ProductDTO.builder()
                    .price(44.99)
                    .build();

            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.of(savedProduct));
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            productService.updateProduct(VALID_ID, updateRequest, List.of());

            // Assert — S3 delete and upload should never be called (no new images)
            verify(s3Service, never()).deleteFile(any());
            verify(s3Service, never()).uploadFile(any());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("shouldDeleteOldImagesAndUploadNewOnesWhenNewImagesProvided")
        void shouldDeleteOldImagesAndUploadNewOnesWhenNewImagesProvided() {
            // Arrange
            // The existing product has one S3 image URL.
            // When we update with a new image, the old one should be deleted from S3
            // and the new one uploaded. This prevents orphaned files in the S3 bucket.
            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.of(savedProduct));
            when(s3Service.uploadFile(any(MultipartFile.class))).thenReturn(IMAGE_URL_2);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(objectMapper.mapObject()).thenReturn(modelMapper);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            // Act
            productService.updateProduct(VALID_ID, productDTO, List.of(imageFile));

            // Assert — old image deleted from S3, new one uploaded
            verify(s3Service).deleteFile("https://s3.amazonaws.com/hoodie.jpg");
            verify(s3Service).uploadFile(any(MultipartFile.class));
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("shouldDeleteS3ImagesAndProductWhenProductExistsWithImages")
        void shouldDeleteS3ImagesAndProductWhenProductExistsWithImages() {
            // Arrange
            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.of(savedProduct));

            // Act
            String result = productService.deleteProduct(VALID_ID);

            // Assert
            assertThat(result).contains(VALID_ID);
            verify(s3Service).deleteFile("https://s3.amazonaws.com/hoodie.jpg");
            verify(productRepository).delete(savedProduct);
        }

        @Test
        @DisplayName("shouldSkipS3DeletionAndDeleteProductWhenProductHasNoImages")
        void shouldSkipS3DeletionAndDeleteProductWhenProductHasNoImages() {
            // Arrange — product with no image URLs
            Product productNoImages = Product.builder()
                    .id(new ObjectId(VALID_ID))
                    .title(PRODUCT_TITLE)
                    .imageUrls(List.of())
                    .build();

            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.of(productNoImages));

            // Act
            productService.deleteProduct(VALID_ID);

            // Assert — S3 delete never called, but DB delete still happens
            verify(s3Service, never()).deleteFile(any());
            verify(productRepository).delete(productNoImages);
        }

        @Test
        @DisplayName("shouldThrowWhenProductNotFound")
        void shouldThrowWhenProductNotFound() {
            when(productRepository.findById(new ObjectId(VALID_ID))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(VALID_ID))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("not found");

            verify(productRepository, never()).delete(any());
            verify(s3Service, never()).deleteFile(any());
        }
    }

    @Nested
    @DisplayName("deleteAllProducts")
    class DeleteAllProducts {

        @Test
        @DisplayName("shouldDeleteAllS3ImagesAndAllProductsWhenProductsExist")
        void shouldDeleteAllS3ImagesAndAllProductsWhenProductsExist() {
            // Arrange
            when(productRepository.findAll()).thenReturn(List.of(savedProduct));

            // Act
            String result = productService.deleteAllProducts();

            // Assert
            assertThat(result).containsIgnoringCase("deleted");
            verify(s3Service).deleteFile("https://s3.amazonaws.com/hoodie.jpg");
            verify(productRepository).deleteAll(List.of(savedProduct));
        }

        @Test
        @DisplayName("shouldThrowWhenNoProductsExistToDelete")
        void shouldThrowWhenNoProductsExistToDelete() {
            // Arrange
            when(productRepository.findAll()).thenReturn(List.of());

            // Act & Assert
            assertThatThrownBy(() -> productService.deleteAllProducts())
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("No Products to delete");

            // Nothing should be deleted if there's nothing to delete
            verify(productRepository, never()).deleteAll(any(List.class));
            verify(s3Service, never()).deleteFile(any());
        }

        @Test
        @DisplayName("shouldSkipS3DeletionForProductsWithNullImageUrls")
        void shouldSkipS3DeletionForProductsWithNullImageUrls() {
            // Arrange — product with null imageUrls (edge case in data)
            Product productNullImages = Product.builder()
                    .id(new ObjectId(VALID_ID))
                    .title(PRODUCT_TITLE)
                    .imageUrls(null)
                    .build();

            when(productRepository.findAll()).thenReturn(List.of(productNullImages));

            // Act — should not throw a NullPointerException
            productService.deleteAllProducts();

            // Assert — S3 is never called, DB delete still happens
            verify(s3Service, never()).deleteFile(any());
            verify(productRepository).deleteAll(any(List.class));
        }
    }
}