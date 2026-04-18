package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.product.GetAllProductsDTO;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.exception.ProductException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductApIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("ProductAPI Controller Tests")
class ProductApIImplTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;

    @MockBean private OTPService otpService;
    @MockBean private UserService userService;
    @MockBean private CartService cartService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private OrderService orderService;
    @MockBean private WishlistService wishlistService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private S3Service s3Service;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private static final String VALID_ID = "507f1f77bcf86cd799439011";
    private static final String PRODUCT_JSON = """
            {
              "title": "Organic Fleece Hoodie",
              "description": "Lightweight hoodie",
              "category": "Clothing",
              "price": 49.99,
              "rate": "4.5",
              "quantity": 25
            }
            """;

    private ProductDTO productDTO;
    private GetAllProductsDTO getAllProductsDTO;
    private MockMultipartFile productPart;
    private MockMultipartFile imagePart;

    @BeforeEach
    void setUp() {
        productDTO = ProductDTO.builder()
                .id(VALID_ID)
                .title("Organic Fleece Hoodie")
                .description("Lightweight hoodie")
                .category("Clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(25)
                .imageUrls(List.of("https://s3.amazonaws.com/hoodie.jpg"))
                .build();
        getAllProductsDTO = GetAllProductsDTO.builder()
                .products(List.of(productDTO))
                .pageNo(0)
                .totalPages(1)
                .totalElements(1L)
                .last(true)
                .build();
        productPart = new MockMultipartFile("product", "", MediaType.APPLICATION_JSON_VALUE, PRODUCT_JSON.getBytes());
        imagePart = new MockMultipartFile("images", "hoodie.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());
    }

    @Nested
    @DisplayName("POST /api/v1/products/product")
    class CreateProduct {
        @Test
        @DisplayName("shouldReturn200WithProductDataWhenRequestIsValid")
        void shouldReturn200WithProductDataWhenRequestIsValid() throws Exception {
            when(productService.addProduct(any(ProductDTO.class), anyList())).thenReturn(productDTO);

            mockMvc.perform(multipart("/api/v1/products")
                            .file(productPart)
                            .file(imagePart)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Product Added"))
                    .andExpect(jsonPath("$.data.title").value("Organic Fleece Hoodie"))
                    .andExpect(jsonPath("$.data.price").value(49.99));
        }

        @Test
        @DisplayName("shouldReturn400WhenServiceThrowsProductException")
        void shouldReturn400WhenServiceThrowsProductException() throws Exception {
            when(productService.addProduct(any(ProductDTO.class), anyList()))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Product creation failed.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(multipart("/api/v1/products")
                            .file(productPart)
                            .file(imagePart)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product creation failed."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetAllProducts {
        @Test
        @DisplayName("shouldReturn200WithProductListWhenProductsExist")
        void shouldReturn200WithProductListWhenProductsExist() throws Exception {
            when(productService.getAllPosts(anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(getAllProductsDTO);

            mockMvc.perform(get("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("All Products retrieved"))
                    .andExpect(jsonPath("$.data.products").isArray())
                    .andExpect(jsonPath("$.data.products[0].title")
                            .value("Organic Fleece Hoodie"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("shouldPassCustomPaginationParamsToService")
        void shouldPassCustomPaginationParamsToService() throws Exception {
            when(productService.getAllPosts(eq(2), eq(5), eq("price"), eq("desc")))
                    .thenReturn(getAllProductsDTO);

            mockMvc.perform(get("/api/v1/products")
                            .param("pageNumber", "2")
                            .param("pageSize", "5")
                            .param("sortBy", "price")
                            .param("sortOrder", "desc"))
                    .andExpect(status().isOk());

            verify(productService).getAllPosts(eq(2), eq(5), eq("price"), eq("desc"));
        }

        @Test
        @DisplayName("shouldReturn400WhenNoProductsFound")
        void shouldReturn400WhenNoProductsFound() throws Exception {
            when(productService.getAllPosts(anyInt(), anyInt(), anyString(), anyString()))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "No products found.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No products found."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProduct {
        @Test
        @DisplayName("shouldReturn200WithProductWhenIdExists")
        void shouldReturn200WithProductWhenIdExists() throws Exception {
            when(productService.getProduct(eq(VALID_ID))).thenReturn(productDTO);

            mockMvc.perform(get("/api/v1/products/{id}", VALID_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Product Retrieved"))
                    .andExpect(jsonPath("$.data.id").value(VALID_ID))
                    .andExpect(jsonPath("$.data.title").value("Organic Fleece Hoodie"));
        }

        @Test
        @DisplayName("shouldReturn400WhenProductNotFound")
        void shouldReturn400WhenProductNotFound() throws Exception {
            when(productService.getProduct(eq(VALID_ID)))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Product with ID " + VALID_ID + " doesn't exist.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(get("/api/v1/products/{id}", VALID_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Product with ID " + VALID_ID + " doesn't exist."));
        }

        @Test
        @DisplayName("shouldReturn400WhenIdFormatIsInvalid")
        void shouldReturn400WhenIdFormatIsInvalid() throws Exception {
            when(productService.getProduct(eq("invalid-id")))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Invalid ID format. ID must be a 24-character hexadecimal string.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(get("/api/v1/products/{id}", "invalid-id"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            "Invalid ID format. ID must be a 24-character hexadecimal string."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/category/{category}")
    class GetProductsByCategory {
        @Test
        @DisplayName("shouldReturn200WithProductsWhenCategoryExists")
        void shouldReturn200WithProductsWhenCategoryExists() throws Exception {
            when(productService.getProductByCategory(
                    eq("Clothing"), anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(getAllProductsDTO);

            mockMvc.perform(get("/api/v1/products/category/{category}", "Clothing"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("List of products with keyword Clothing."))
                    .andExpect(jsonPath("$.data.products").isArray());
        }

        @Test
        @DisplayName("shouldPassCategoryPathVariableToService")
        void shouldPassCategoryPathVariableToService() throws Exception {
            when(productService.getProductByCategory(
                    eq("Electronics"), anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(getAllProductsDTO);

            mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics"))
                    .andExpect(status().isOk());

            verify(productService).getProductByCategory(
                    eq("Electronics"), anyInt(), anyInt(), anyString(), anyString());
        }

        @Test
        @DisplayName("shouldReturn400WhenCategoryNotFound")
        void shouldReturn400WhenCategoryNotFound() throws Exception {
            when(productService.getProductByCategory(
                    eq("UnknownCategory"), anyInt(), anyInt(), anyString(), anyString()))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "No products with category 'UnknownCategory' were found.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(get("/api/v1/products/category/{category}", "UnknownCategory"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("No products with category 'UnknownCategory' were found."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/title/{title}")
    class GetProductsByTitle {
        @Test
        @DisplayName("shouldReturn200WithProductsWhenTitleMatches")
        void shouldReturn200WithProductsWhenTitleMatches() throws Exception {
            when(productService.getProductByTitle(
                    eq("Hoodie"), anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(getAllProductsDTO);

            mockMvc.perform(get("/api/v1/products/title/{title}", "Hoodie"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("List of products with keyword Hoodie."))
                    .andExpect(jsonPath("$.data.products").isArray());
        }

        @Test
        @DisplayName("shouldReturn400WhenTitleNotFound")
        void shouldReturn400WhenTitleNotFound() throws Exception {
            when(productService.getProductByTitle(
                    eq("Unknown"), anyInt(), anyInt(), anyString(), anyString()))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "No products with title 'Unknown' were found.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(get("/api/v1/products/title/{title}", "Unknown"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("No products with title 'Unknown' were found."));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/products/product/{productId}")
    class UpdateProduct {
        @Test
        @DisplayName("shouldReturn200WithUpdatedProductWhenRequestIsValid")
        void shouldReturn200WithUpdatedProductWhenRequestIsValid() throws Exception {
            when(productService.updateProduct(eq(VALID_ID), any(ProductDTO.class), anyList()))
                    .thenReturn(productDTO);

            mockMvc.perform(multipart("/api/v1/products/product/{productId}", VALID_ID)
                            .file(productPart)
                            .file(imagePart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Product Updated."))
                    .andExpect(jsonPath("$.data.title").value("Organic Fleece Hoodie"));
        }

        @Test
        @DisplayName("shouldReturn200WhenUpdatingWithoutImages")
        void shouldReturn200WhenUpdatingWithoutImages() throws Exception {
            when(productService.updateProduct(eq(VALID_ID), any(ProductDTO.class), any()))
                    .thenReturn(productDTO);

            mockMvc.perform(multipart("/api/v1/products/product/{productId}", VALID_ID)
                            .file(productPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Product Updated."));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/product/{productId}")
    class DeleteProduct {
        @Test
        @DisplayName("shouldReturn400WhenProductNotFound")
        void shouldReturn400WhenProductNotFound() throws Exception {
            doThrow(new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "Product with id '" + VALID_ID + "' not found.",
                    HttpStatus.BAD_REQUEST.value()))
                    .when(productService).deleteProduct(eq(VALID_ID));

            mockMvc.perform(delete("/api/v1/products/product/{productId}", VALID_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Product with id '" + VALID_ID + "' not found."));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/product")
    class DeleteAllProduct {
        @Test
        @DisplayName("shouldReturn400WhenNoProductsExistToDelete")
        void shouldReturn400WhenNoProductsExistToDelete() throws Exception {
            doThrow(new ProductException(
                    HttpStatus.BAD_REQUEST.toString(),
                    "No Products to delete",
                    HttpStatus.BAD_REQUEST.value()))
                    .when(productService).deleteAllProducts();

            mockMvc.perform(delete("/api/v1/products/product"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No Products to delete"));
        }
    }
}