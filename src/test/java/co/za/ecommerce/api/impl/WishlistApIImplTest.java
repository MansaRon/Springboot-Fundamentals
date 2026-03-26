package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.product.ProductDTO;
import co.za.ecommerce.dto.wishlist.WishlistDTO;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.exception.WishlistException;
import co.za.ecommerce.mapper.ObjectMapper;
import co.za.ecommerce.security.CustomUserDetailsService;
import co.za.ecommerce.security.JwtTokenProvider;
import org.bson.types.ObjectId;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistApIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("WishlistApIImpl Controller Tests")
class WishlistApIImplTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private WishlistService wishlistService;

    @MockBean private OTPService otpService;
    @MockBean private UserService userService;
    @MockBean private ProductService productService;
    @MockBean private CartService cartService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private OrderService orderService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private S3Service s3Service;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private static final String USER_ID = "507f1f77bcf86cd799439011";
    private static final String PRODUCT_ID = "507f1f77bcf86cd799439022";

    private WishlistDTO wishlistDTO;
    private WishlistDTO savedWishlistDTO;

    @BeforeEach
    void setUp() {
        ProductDTO productDTO = ProductDTO.builder()
                .id(PRODUCT_ID)
                .title("Organic Fleece Hoodie")
                .description("Lightweight hoodie")
                .category("Clothing")
                .price(49.99)
                .rate("4.5")
                .quantity(25)
                .build();

        wishlistDTO = WishlistDTO.builder()
                .userID(new ObjectId(USER_ID))
                .productID(new ObjectId(PRODUCT_ID))
                .productDTO(productDTO)
                .build();

        savedWishlistDTO = WishlistDTO.builder()
                .id("507f1f77bcf86cd799439099")
                .userID(new ObjectId(USER_ID))
                .productID(new ObjectId(PRODUCT_ID))
                .productDTO(productDTO)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/wishlist")
    class AddWishlist {
        @Test
        @DisplayName("shouldReturn200WithSavedWishlistWhenRequestIsValid")
        void shouldReturn200WithSavedWishlistWhenRequestIsValid() throws Exception {
            when(wishlistService.add(any(WishlistDTO.class))).thenReturn(savedWishlistDTO);

            mockMvc.perform(post("/api/v1/wishlist")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userID": "%s",
                                      "productID": "%s",
                                      "productDTO": {
                                        "title": "Organic Fleece Hoodie",
                                        "description": "Lightweight hoodie",
                                        "category": "Clothing",
                                        "price": 49.99,
                                        "rate": "4.5",
                                        "quantity": 25
                                      }
                                    }
                                    """.formatted(USER_ID, PRODUCT_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Wishlist item added."))
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }

        @Test
        @DisplayName("shouldReturn400WhenServiceThrowsWishlistException")
        void shouldReturn400WhenServiceThrowsWishlistException() throws Exception {
            when(wishlistService.add(any(WishlistDTO.class)))
                    .thenThrow(new WishlistException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Wishlist item already exists.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(post("/api/v1/wishlist")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userID": "%s",
                                      "productID": "%s",
                                      "productDTO": {
                                        "title": "Organic Fleece Hoodie",
                                        "description": "Lightweight hoodie",
                                        "category": "Clothing",
                                        "price": 49.99,
                                        "rate": "4.5",
                                        "quantity": 25
                                      }
                                    }
                                    """.formatted(USER_ID, PRODUCT_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Wishlist item already exists."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/wishlist")
    class GetWishlist {
        @Test
        @DisplayName("shouldReturn200WithWishlistItemsWhenUserHasItems")
        void shouldReturn200WithWishlistItemsWhenUserHasItems() throws Exception {
            when(wishlistService.findAll(eq(USER_ID)))
                    .thenReturn(List.of(savedWishlistDTO));

            mockMvc.perform(get("/api/v1/wishlist")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Wishlist retrieved."))
                    .andExpect(jsonPath("$.dataList").isArray())
                    .andExpect(jsonPath("$.dataList.length()").value(1));
        }

        @Test
        @DisplayName("shouldReturn200WithEmptyListWhenUserHasNoItems")
        void shouldReturn200WithEmptyListWhenUserHasNoItems() throws Exception {
            when(wishlistService.findAll(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/wishlist")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList").isArray())
                    .andExpect(jsonPath("$.dataList").isEmpty());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/wishlist/{userID}")
    class DeleteWishlist {
        @Test
        @DisplayName("shouldReturn200WithDeleteConfirmationWhenItemExists")
        void shouldReturn200WithDeleteConfirmationWhenItemExists() throws Exception {
            when(wishlistService.delete(eq(USER_ID), any(WishlistDTO.class)))
                    .thenReturn("Wishlist item deleted");

            mockMvc.perform(delete("/api/v1/wishlist/{userID}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userID": "%s",
                                      "productID": "%s",
                                      "productDTO": {
                                        "title": "Organic Fleece Hoodie",
                                        "description": "Lightweight hoodie",
                                        "category": "Clothing",
                                        "price": 49.99,
                                        "rate": "4.5",
                                        "quantity": 25
                                      }
                                    }
                                    """.formatted(USER_ID, PRODUCT_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Wishlist item deleted."))
                    .andExpect(jsonPath("$.dataDelete").value("Wishlist item deleted"));
        }

        @Test
        @DisplayName("shouldReturn400WhenWishlistItemNotFound")
        void shouldReturn400WhenWishlistItemNotFound() throws Exception {
            when(wishlistService.delete(any(), any()))
                    .thenThrow(new WishlistException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Wishlist item not found for the given user and product.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(delete("/api/v1/wishlist/{userID}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userID": "%s",
                                      "productID": "%s",
                                      "productDTO": {
                                        "title": "Organic Fleece Hoodie",
                                        "description": "Lightweight hoodie",
                                        "category": "Clothing",
                                        "price": 49.99,
                                        "rate": "4.5",
                                        "quantity": 25
                                      }
                                    }
                                    """.formatted(USER_ID, PRODUCT_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Wishlist item not found for the given user and product."));
        }
    }
}