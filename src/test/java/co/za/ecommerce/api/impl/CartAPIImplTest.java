package co.za.ecommerce.api.impl;

import co.za.ecommerce.business.*;
import co.za.ecommerce.dto.cart.CartDTO;
import co.za.ecommerce.dto.cart.CartItemsDTO;
import co.za.ecommerce.exception.CartException;
import co.za.ecommerce.exception.GlobalExceptionHandler;
import co.za.ecommerce.exception.ProductException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartAPIImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("CartAPI Controller Tests")
class CartAPIImplTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CartService cartService;

    @MockBean private OTPService otpService;
    @MockBean private UserService userService;
    @MockBean private ProductService productService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private OrderService orderService;
    @MockBean private WishlistService wishlistService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private S3Service s3Service;
    @MockBean private ObjectMapper objectMapper;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private static final String USER_ID = "507f1f77bcf86cd799439011";
    private static final String PRODUCT_ID = "507f1f77bcf86cd799439022";

    private CartDTO cartDTO;

    @BeforeEach
    void setUp() {
        CartItemsDTO item = CartItemsDTO.builder()
                .quantity(2)
                .productPrice(99.98)
                .discount(0)
                .tax(0)
                .build();

        cartDTO = CartDTO.builder()
                .cartItems(List.of(item))
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/cart/{userId}/add-item/{productId}")
    class AddProductToCart {
        @Test
        @DisplayName("shouldReturn200WithCartDataWhenItemAddedSuccessfully")
        void shouldReturn200WithCartDataWhenItemAddedSuccessfully() throws Exception {
            when(cartService.addProductToCart(
                    eq(new ObjectId(USER_ID)),
                    eq(new ObjectId(PRODUCT_ID)),
                    eq(2)))
                    .thenReturn(cartDTO);

            mockMvc.perform(post("/api/v1/cart/{userId}/add-item/{productId}", USER_ID, PRODUCT_ID)
                            .param("quantity", "2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Product Added To Cart"))
                    .andExpect(jsonPath("$.data.cartItems").isArray())
                    .andExpect(jsonPath("$.data.cartItems[0].quantity").value(2));
        }

        @Test
        @DisplayName("shouldPassCorrectPathVariablesAndQuantityToService")
        void shouldPassCorrectPathVariablesAndQuantityToService() throws Exception {
            when(cartService.addProductToCart(any(), any(), anyInt())).thenReturn(cartDTO);

            mockMvc.perform(post("/api/v1/cart/{userId}/add-item/{productId}", USER_ID, PRODUCT_ID)
                            .param("quantity", "5"))
                    .andExpect(status().isOk());

            verify(cartService).addProductToCart(
                    eq(new ObjectId(USER_ID)),
                    eq(new ObjectId(PRODUCT_ID)),
                    eq(5));
        }

        @Test
        @DisplayName("shouldReturn400WhenProductIsOutOfStock")
        void shouldReturn400WhenProductIsOutOfStock() throws Exception {
            when(cartService.addProductToCart(any(), any(), anyInt()))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Organic Fleece Hoodie is not available.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(post("/api/v1/cart/{userId}/add-item/{productId}", USER_ID, PRODUCT_ID)
                            .param("quantity", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Organic Fleece Hoodie is not available."));
        }

        @Test
        @DisplayName("shouldReturn400WhenRequestedQuantityExceedsStock")
        void shouldReturn400WhenRequestedQuantityExceedsStock() throws Exception {
            when(cartService.addProductToCart(any(), any(), anyInt()))
                    .thenThrow(new ProductException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Please order Organic Fleece Hoodie in a quantity less than or equal to 5.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(post("/api/v1/cart/{userId}/add-item/{productId}", USER_ID, PRODUCT_ID)
                            .param("quantity", "10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            "Please order Organic Fleece Hoodie in a quantity less than or equal to 5."));
        }

        @Test
        @DisplayName("shouldReturn400WhenProductNotFound")
        void shouldReturn400WhenProductNotFound() throws Exception {
            when(cartService.addProductToCart(any(), any(), anyInt()))
                    .thenThrow(new ProductException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Product not found.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(post("/api/v1/cart/{userId}/add-item/{productId}", USER_ID, PRODUCT_ID)
                            .param("quantity", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product not found."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cart/{userId}")
    class GetUserCartWithItems {

        @Test
        @DisplayName("shouldReturn200WithCartWhenUserExists")
        void shouldReturn200WithCartWhenUserExists() throws Exception {
            when(cartService.getUserCartWithItems(eq(new ObjectId(USER_ID))))
                    .thenReturn(cartDTO);

            mockMvc.perform(get("/api/v1/cart/{userId}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message")
                            .value("Cart with user ID " + USER_ID + " retrieved."))
                    .andExpect(jsonPath("$.data.cartItems").isArray());
        }

        @Test
        @DisplayName("shouldReturn200WithEmptyCartWhenCartIsNewlyCreated")
        void shouldReturn200WithEmptyCartWhenCartIsNewlyCreated() throws Exception {
            // getUserCartWithItems silently creates a cart if none exists
            CartDTO emptyCart = CartDTO.builder()
                    .cartItems(List.of())
                    .build();

            when(cartService.getUserCartWithItems(eq(new ObjectId(USER_ID))))
                    .thenReturn(emptyCart);

            mockMvc.perform(get("/api/v1/cart/{userId}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.cartItems").isEmpty());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cart/{userId}/update-item/{productId}")
    class UpdateProductInCart {

        @Test
        @DisplayName("shouldReturn200WithUpdatedCartWhenQuantityChanged")
        void shouldReturn200WithUpdatedCartWhenQuantityChanged() throws Exception {
            when(cartService.updateProductInCart(
                    eq(new ObjectId(USER_ID)),
                    eq(new ObjectId(PRODUCT_ID)),
                    eq(5)))
                    .thenReturn(cartDTO);

            mockMvc.perform(patch("/api/v1/cart/{userId}/update-item/{productId}",
                            USER_ID, PRODUCT_ID)
                            .param("newQuantity", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Cart with user ID " + USER_ID + " updated."));
        }

        @Test
        @DisplayName("shouldPassCorrectPathVariablesAndNewQuantityToService")
        void shouldPassCorrectPathVariablesAndNewQuantityToService() throws Exception {
            when(cartService.updateProductInCart(any(), any(), anyInt())).thenReturn(cartDTO);

            mockMvc.perform(patch("/api/v1/cart/{userId}/update-item/{productId}",
                            USER_ID, PRODUCT_ID)
                            .param("newQuantity", "3"))
                    .andExpect(status().isOk());

            verify(cartService).updateProductInCart(
                    eq(new ObjectId(USER_ID)),
                    eq(new ObjectId(PRODUCT_ID)),
                    eq(3));
        }

        @Test
        @DisplayName("shouldReturn200WhenNewQuantityIsZeroRemovingItem")
        void shouldReturn200WhenNewQuantityIsZeroRemovingItem() throws Exception {
            CartDTO emptyCart = CartDTO.builder()
                    .cartItems(List.of())
                    .build();

            when(cartService.updateProductInCart(any(), any(), eq(0))).thenReturn(emptyCart);

            mockMvc.perform(patch("/api/v1/cart/{userId}/update-item/{productId}",
                            USER_ID, PRODUCT_ID)
                            .param("newQuantity", "0"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("shouldReturn400WhenCartNotFound")
        void shouldReturn400WhenCartNotFound() throws Exception {
            when(cartService.updateProductInCart(any(), any(), anyInt()))
                    .thenThrow(new CartException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Cart not found for user.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(patch("/api/v1/cart/{userId}/update-item/{productId}",
                            USER_ID, PRODUCT_ID)
                            .param("newQuantity", "3"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cart not found for user."));
        }

        @Test
        @DisplayName("shouldReturn400WhenProductNotFoundInCart")
        void shouldReturn400WhenProductNotFoundInCart() throws Exception {
            when(cartService.updateProductInCart(any(), any(), anyInt()))
                    .thenThrow(new CartException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Product not found in cart.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(patch("/api/v1/cart/{userId}/update-item/{productId}",
                            USER_ID, PRODUCT_ID)
                            .param("newQuantity", "3"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product not found in cart."));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cart/{userId}/delete-item/{productId}")
    class DeleteProductFromCart {
        @Test
        @DisplayName("shouldReturn200WithUpdatedCartWhenItemDeleted")
        void shouldReturn200WithUpdatedCartWhenItemDeleted() throws Exception {
            CartDTO cartAfterDeletion = CartDTO.builder()
                    .cartItems(List.of())
                    .build();

            when(cartService.deleteProductFromCart(
                    eq(new ObjectId(USER_ID)),
                    eq(new ObjectId(PRODUCT_ID))))
                    .thenReturn(cartAfterDeletion);

            mockMvc.perform(delete("/api/v1/cart/{userId}/delete-item/{productId}",
                            USER_ID, PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message")
                            .value("Cart with user ID " + USER_ID + " deleted."))
                    .andExpect(jsonPath("$.data.cartItems").isEmpty());
        }

        @Test
        @DisplayName("shouldPassCorrectPathVariablesToService")
        void shouldPassCorrectPathVariablesToService() throws Exception {
            when(cartService.deleteProductFromCart(any(), any())).thenReturn(cartDTO);

            mockMvc.perform(delete("/api/v1/cart/{userId}/delete-item/{productId}",
                    USER_ID, PRODUCT_ID));

            verify(cartService).deleteProductFromCart(
                    eq(new ObjectId(USER_ID)),
                    eq(new ObjectId(PRODUCT_ID)));
        }

        @Test
        @DisplayName("shouldReturn400WhenCartNotFound")
        void shouldReturn400WhenCartNotFound() throws Exception {
            when(cartService.deleteProductFromCart(any(), any()))
                    .thenThrow(new CartException(
                            HttpStatus.BAD_REQUEST.toString(),
                            "Cart not found for user.",
                            HttpStatus.BAD_REQUEST.value()));

            mockMvc.perform(delete("/api/v1/cart/{userId}/delete-item/{productId}",
                            USER_ID, PRODUCT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cart not found for user."));
        }

        @Test
        @DisplayName("shouldReturn400WhenProductNotFoundInCart")
        void shouldReturn400WhenProductNotFoundInCart() throws Exception {
            when(cartService.deleteProductFromCart(any(), any()))
                    .thenThrow(new CartException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Product not found in cart.",
                            HttpStatus.NOT_FOUND.value()));

            mockMvc.perform(delete("/api/v1/cart/{userId}/delete-item/{productId}",
                            USER_ID, PRODUCT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product not found in cart."));
        }
    }
}